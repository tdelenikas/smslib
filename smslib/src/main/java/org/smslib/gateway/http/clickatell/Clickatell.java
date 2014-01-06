
package org.smslib.gateway.http.clickatell;

import java.io.IOException;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.core.Capabilities;
import org.smslib.core.Capabilities.Caps;
import org.smslib.gateway.AbstractGateway;
import org.smslib.gateway.http.AbstractHttpGateway;
import org.smslib.helper.Common;
import org.smslib.message.DeliveryReportMessage;
import org.smslib.message.DeliveryReportMessage.DeliveryStatus;
import org.smslib.message.OutboundMessage;
import org.smslib.message.OutboundMessage.FailureCause;
import org.smslib.message.OutboundMessage.SentStatus;

public class Clickatell extends AbstractHttpGateway
{
	static Logger logger = LoggerFactory.getLogger(Clickatell.class);

	String apiId;

	String username;

	String password;

	String sessionId;

	Date lastAuth;

	public Clickatell(String gatewayId, String apiId, String username, String password)
	{
		super(gatewayId, "Clickatell (http://www.clickatell.com)");
		Capabilities caps = new Capabilities();
		caps.set(Caps.CanSendMessage);
		caps.set(Caps.CanSendFlashMessage);
		caps.set(Caps.CanSetSenderId);
		caps.set(Caps.CanSplitMessages);
		caps.set(Caps.CanQueryCreditBalance);
		caps.set(Caps.CanQueryDeliveryStatus);
		caps.set(Caps.CanRequestDeliveryStatus);
		setCapabilities(caps);
		setBaseUrl("");
		setSubmitMessageUrl("https://api.clickatell.com/http/sendmsg");
		setQueryMessageUrl("https://api.clickatell.com/http/querymsg");
		setQueryBalanceUrl("https://api.clickatell.com/http/getbalance");
		setQueryCoverageUrl("https://api.clickatell.com/utils/routeCoverage.php");
		setHttpMethod(HttpMethod.POST);
		setHttpEncoding("UTF-8");
		setMaxMessageParts(1);
		this.apiId = apiId;
		this.username = username;
		this.password = password;
		this.sessionId = "";
		this.lastAuth = Common.getMinDate();
	}

	public Clickatell(String gatewayId, String... parms)
	{
		this(gatewayId, parms[0], parms[1], parms[2]);
	}

	@Override
	protected void prepareUrlConnection(URLConnection con)
	{
		// Nothing here on purpose!
	}

	@Override
	protected void prepareParameters(Operation operation, Object o, Hashtable<String, String> parameters, Object... args)
	{
		OutboundMessage message;
		try
		{
			if (Common.isNullOrEmpty(this.sessionId)) authorize();
			else
			{
				Calendar calCurrent = Calendar.getInstance();
				Calendar calLastAuth = Calendar.getInstance();
				calCurrent.setTime(new Date());
				calLastAuth.setTime(this.lastAuth);
				long msCurrent = calCurrent.getTimeInMillis();
				long msLastAuth = calLastAuth.getTimeInMillis();
				long diffMinutes = (msCurrent - msLastAuth) / (60 * 1000);
				if (diffMinutes >= 10) authorize();
			}
		}
		catch (Exception e)
		{
			logger.error("Could not get Clickatell session!", e);
			this.sessionId = "";
		}
		switch (operation)
		{
			case SendMessage:
				int requestFeatures = 1;
				message = (OutboundMessage) o;
				if (!message.getOriginatorAddress().isVoid())
				{
					parameters.put("from", message.getOriginatorAddress().getAddress());
					requestFeatures += 16;
					requestFeatures += 32;
				}
				else if (!getSenderAddress().isVoid())
				{
					parameters.put("from", getSenderAddress().getAddress());
					requestFeatures += 16;
					requestFeatures += 32;
				}
				parameters.put("session_id", this.sessionId);
				parameters.put("to", message.getRecipientAddress().getAddress());
				parameters.put("text", translateText(message.getPayload().getText()));
				parameters.put("concat", Integer.toString(getMaxMessageParts()));
				parameters.put("climsgid", message.getId());
				if (message.getRequestDeliveryReport() || getRequestDeliveryReport()) requestFeatures += 8192;
				if (message.getPriority() < 0) parameters.put("queue", "3");
				else if (message.getPriority() == 0) parameters.put("queue", "2");
				else parameters.put("queue", "1");
				if (message.isFlashSms())
				{
					parameters.put("msg_type", "SMS_FLASH");
					requestFeatures += 512;
				}
				parameters.put("req_feat", String.valueOf(requestFeatures));
				break;
			case QueryBalance:
				parameters.put("session_id", this.sessionId);
				break;
			case QueryMessage:
				parameters.put("session_id", this.sessionId);
				parameters.put("apimsgid", ((DeliveryReportMessage) o).getOperatorMessageId());
				break;
			default:
				throw new RuntimeException("Not implemented!");
		}
	}

	@Override
	protected void parseResponse(Operation operation, Object o, List<String> responseList) throws Exception
	{
		StringTokenizer tokens;
		String t1, t2, t3;
		tokens = new StringTokenizer(responseList.get(0), ":,");
		t1 = tokens.nextToken().trim();
		t2 = (tokens.hasMoreTokens() ? tokens.nextToken().trim() : "");
		t3 = (tokens.hasMoreTokens() ? tokens.nextToken().trim() : "");
		switch (operation)
		{
			case SendMessage:
				OutboundMessage message1 = (OutboundMessage) o;
				if (t1.equalsIgnoreCase("ID"))
				{
					message1.getOperatorMessageIds().add(t2);
					message1.setGatewayId(getGatewayId());
					message1.setSentDate(new Date());
					message1.setSentStatus(SentStatus.Sent);
					message1.setFailureCause(FailureCause.None);
				}
				else
				{
					message1.setSentStatus(SentStatus.Failed);
					message1.setOperatorFailureCode(t2);
					switch (Integer.parseInt(t2))
					{
						case 1:
						case 2:
						case 3:
						case 4:
						case 5:
						case 6:
						case 7:
							message1.setFailureCause(FailureCause.AuthFailure);
							break;
						case 101:
						case 102:
						case 105:
						case 106:
						case 107:
						case 112:
						case 116:
						case 120:
							message1.setFailureCause(FailureCause.BadFormat);
							break;
						case 301:
						case 302:
							message1.setFailureCause(FailureCause.NoCredit);
							break;
						case 114:
							message1.setFailureCause(FailureCause.NoRoute);
							break;
						default:
							message1.setFailureCause(FailureCause.UnknownFailure);
					}
				}
				break;
			case QueryBalance:
				if (t1.equalsIgnoreCase("CREDIT")) ((AbstractGateway) o).getCreditBalance().setCredits(Double.parseDouble(t2));
				break;
			case QueryMessage:
				DeliveryReportMessage message2 = (DeliveryReportMessage) o;
				if (t1.equalsIgnoreCase("ERR")) message2.setDeliveryStatus(DeliveryStatus.Error);
				else
				{
					switch (Integer.parseInt(t3))
					{
						case 1:
							message2.setDeliveryStatus(DeliveryStatus.Unknown);
							break;
						case 2:
						case 3:
						case 8:
						case 11:
							message2.setDeliveryStatus(DeliveryStatus.Pending);
							break;
						case 4:
							message2.setDeliveryStatus(DeliveryStatus.Delivered);
							break;
						case 5:
						case 6:
						case 7:
						case 9:
						case 10:
						case 12:
							message2.setDeliveryStatus(DeliveryStatus.Failed);
							break;
						default:
							message2.setDeliveryStatus(DeliveryStatus.Unknown);
							break;
					}
				}
				break;
			default:
				throw new RuntimeException("Not implemented!");
		}
	}

	protected boolean authorize() throws IOException
	{
		Hashtable<String, String> parms = new Hashtable<>();
		parms.put("api_id", this.apiId);
		parms.put("user", this.username);
		parms.put("password", this.password);
		List<String> responseList = performHttpRequest(HttpMethod.POST, "https://api.clickatell.com/http/auth", parms);
		StringTokenizer tokens = new StringTokenizer(responseList.get(0), ":");
		String t1 = tokens.nextToken().trim();
		String t2 = tokens.nextToken().trim();
		if (t1.equalsIgnoreCase("OK"))
		{
			this.sessionId = t2;
			this.lastAuth = new Date();
		}
		else this.sessionId = "";
		return (this.sessionId.length() != 0);
	}

	@Override
	protected String translateText(String text)
	{
		return text;
	}
}
