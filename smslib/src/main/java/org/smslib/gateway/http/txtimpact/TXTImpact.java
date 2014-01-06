
package org.smslib.gateway.http.txtimpact;

import java.net.URLConnection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import org.smslib.core.Capabilities;
import org.smslib.core.Capabilities.Caps;
import org.smslib.gateway.AbstractGateway;
import org.smslib.gateway.http.AbstractHttpGateway;
import org.smslib.message.OutboundMessage;
import org.smslib.message.OutboundMessage.FailureCause;
import org.smslib.message.OutboundMessage.SentStatus;

public class TXTImpact extends AbstractHttpGateway
{
	String userId;

	String password;

	String vasId;

	String shortCode;

	public TXTImpact(String gatewayId, String userId, String password, String vasId, String shortCode)
	{
		super(gatewayId, "TXTImpact (http://www.txtimpact.com)");
		Capabilities caps = new Capabilities();
		caps.set(Caps.CanSendMessage);
		caps.set(Caps.CanSplitMessages);
		caps.set(Caps.CanQueryCreditBalance);
		setCapabilities(caps);
		setBaseUrl("");
		setSubmitMessageUrl("https://mzone.txtimpact.com/smsadmin/submitsm.aspx");
		setQueryBalanceUrl("http://smsapi.wire2air.com/smsadmin/checksmscredits.aspx");
		setHttpMethod(HttpMethod.POST);
		setHttpEncoding("ISO-8859-1");
		setMaxMessageParts(1);
		this.userId = userId;
		this.password = password;
		this.vasId = vasId;
		this.shortCode = shortCode;
	}

	public TXTImpact(String gatewayId, String... parms)
	{
		this(gatewayId, parms[0], parms[1], parms[2], parms[3]);
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
		switch (operation)
		{
			case SendMessage:
				message = (OutboundMessage) o;
				parameters.put("version", "2.0");
				parameters.put("userid", this.userId);
				parameters.put("password", this.password);
				parameters.put("vasid", this.vasId);
				parameters.put("from", this.shortCode);
				parameters.put("to", message.getRecipientAddress().getAddress());
				parameters.put("text", translateText(message.getPayload().getText()));
				break;
			case QueryBalance:
				parameters.put("userid", this.userId);
				parameters.put("password", this.password);
				parameters.put("vasid", this.vasId);
				break;
			default:
				throw new RuntimeException("Not implemented!");
		}
	}

	@Override
	protected void parseResponse(Operation operation, Object o, List<String> responseList) throws Exception
	{
		OutboundMessage message;
		StringTokenizer tokens;
		String t1, t2, t3;
		tokens = new StringTokenizer(responseList.get(0), ":");
		t1 = tokens.nextToken().trim();
		t2 = (tokens.hasMoreTokens() ? tokens.nextToken().trim() : "");
		t3 = (tokens.hasMoreTokens() ? tokens.nextToken().trim() : "");
		switch (operation)
		{
			case SendMessage:
				message = (OutboundMessage) o;
				if (t1.equalsIgnoreCase("JOBID"))
				{
					message.setGatewayId(getGatewayId());
					message.setSentDate(new Date());
					message.getOperatorMessageIds().add(t3);
					message.setSentStatus(SentStatus.Sent);
					message.setFailureCause(FailureCause.None);
				}
				else
				{
					message.setSentStatus(SentStatus.Failed);
					message.setOperatorFailureCode(t2);
					switch (Integer.parseInt(t2))
					{
						case 301:
							message.setFailureCause(FailureCause.AuthFailure);
							break;
						case 305:
							message.setFailureCause(FailureCause.NoCredit);
							break;
						default:
							message.setFailureCause(FailureCause.UnknownFailure);
					}
				}
				break;
			case QueryBalance:
				String response = responseList.get(0);
				if (response.startsWith("ERR")) response = "0";
				((AbstractGateway) o).getCreditBalance().setCredits(Double.parseDouble(response));
				break;
			default:
				throw new RuntimeException("Not implemented!");
		}
	}

	@Override
	protected String translateText(String text)
	{
		return text;
	}
}
