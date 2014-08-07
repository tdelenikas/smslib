
package org.smslib.gateway.http.textmagic;

import java.net.URLConnection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.core.Capabilities;
import org.smslib.core.Capabilities.Caps;
import org.smslib.gateway.AbstractGateway;
import org.smslib.gateway.http.AbstractHttpGateway;
import org.smslib.message.AbstractMessage.Encoding;
import org.smslib.message.DeliveryReportMessage;
import org.smslib.message.DeliveryReportMessage.DeliveryStatus;
import org.smslib.message.OutboundMessage;
import org.smslib.message.OutboundMessage.FailureCause;
import org.smslib.message.OutboundMessage.SentStatus;

public class TextMagic extends AbstractHttpGateway
{
	static Logger logger = LoggerFactory.getLogger(TextMagic.class);

	String userId;

	String password;

	String vasId;

	String shortCode;

	public TextMagic(String gatewayId, String userId, String password)
	{
		super(gatewayId, "TextMagic (http://www.textmagic.com)");
		Capabilities caps = new Capabilities();
		caps.set(Caps.CanSendMessage);
		caps.set(Caps.CanSetSenderId);
		caps.set(Caps.CanSplitMessages);
		caps.set(Caps.CanQueryCreditBalance);
		caps.set(Caps.CanSendUnicodeMessage);
		caps.set(Caps.CanQueryDeliveryStatus);
		setCapabilities(caps);
		setBaseUrl("https://www.textmagic.com/app/api");
		setHttpMethod(HttpMethod.POST);
		setHttpEncoding("UTF-8");
		setMaxMessageParts(1);
		this.userId = userId;
		this.password = password;
	}

	public TextMagic(String gatewayId, String... parms)
	{
		this(gatewayId, parms[0], parms[1]);
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
				parameters.put("username", this.userId);
				parameters.put("password", this.password);
				parameters.put("cmd", "send");
				parameters.put("phone", message.getRecipientAddress().getAddress());
				parameters.put("text", translateText(message.getPayload().getText()));
				parameters.put("unicode", (message.getEncoding() == Encoding.EncUcs2 ? "1" : "0"));
				if (!message.getOriginatorAddress().isVoid()) parameters.put("from", message.getOriginatorAddress().getAddress());
				else if (!getSenderAddress().isVoid()) parameters.put("from", getSenderAddress().getAddress());
				parameters.put("max_length", String.valueOf(getMaxMessageParts()));
				break;
			case QueryBalance:
				parameters.put("username", this.userId);
				parameters.put("password", this.password);
				parameters.put("cmd", "account");
				break;
			case QueryMessage:
				parameters.put("username", this.userId);
				parameters.put("password", this.password);
				parameters.put("cmd", "message_status");
				parameters.put("ids", ((DeliveryReportMessage) o).getOperatorMessageId());
				break;
			default:
				throw new RuntimeException("Not implemented!");
		}
	}

	@Override
	protected void parseResponse(Operation operation, Object o, List<String> responseList) throws Exception
	{
		StringBuffer responseBuffer = new StringBuffer();
		for (int i = 0; i < responseList.size(); i++)
			responseBuffer.append(responseList.get(i));
		String response = responseBuffer.toString();
		JSONObject responseObject;
		switch (operation)
		{
			case SendMessage:
				OutboundMessage message = (OutboundMessage) o;
				int error;
				try
				{
					responseObject = new JSONObject(response);
					try
					{
						error = responseObject.getInt("error_code");
					}
					catch (JSONException e)
					{
						error = 0;
					}
					if (error > 0)
					{
						message.setSentStatus(SentStatus.Failed);
						message.setOperatorFailureCode(String.valueOf(error));
						switch (error)
						{
							case 1:
							case 4:
								message.setFailureCause(FailureCause.MissingParms);
								break;
							case 3:
							case 7:
							case 10:
							case 12:
								message.setFailureCause(FailureCause.BadFormat);
								break;
							case 2:
								message.setFailureCause(FailureCause.NoCredit);
								break;
							case 5:
							case 13:
								message.setFailureCause(FailureCause.AuthFailure);
								break;
							case 6:
							case 8:
								message.setFailureCause(FailureCause.GatewayFailure);
								break;
							case 9:
								message.setFailureCause(FailureCause.BadNumber);
								break;
							case 11:
								message.setFailureCause(FailureCause.OverQuota);
								break;
							case 14:
							case 15:
								message.setFailureCause(FailureCause.GatewayFailure);
								break;
						}
					}
					else
					{
						message.setGatewayId(getGatewayId());
						message.setSentDate(new Date());
						message.setSentStatus(SentStatus.Sent);
						message.setFailureCause(FailureCause.None);
						JSONObject messageIds = responseObject.getJSONObject("message_id");
						@SuppressWarnings("unchecked")
						Iterator<String> messageIdIterator = messageIds.keys();
						while (messageIdIterator.hasNext())
							message.getOperatorMessageIds().add(messageIdIterator.next());
					}
				}
				catch (JSONException e)
				{
					logger.error("Error parsing response!", e);
					message.setSentStatus(SentStatus.Failed);
					message.setFailureCause(FailureCause.UnknownFailure);
				}
				break;
			case QueryBalance:
				try
				{
					responseObject = new JSONObject(response);
					double balance;
					try
					{
						balance = responseObject.getDouble("balance");
					}
					catch (JSONException e)
					{
						logger.error("Error parsing response, wrong password?", e);
						balance = 0;
					}
					((AbstractGateway) o).getCreditBalance().setCredits(balance);
				}
				catch (JSONException e)
				{
					logger.error("Error parsing response!", e);
					((AbstractGateway) o).getCreditBalance().setCredits(0);
				}
				break;
			case QueryMessage:
				DeliveryReportMessage message2 = (DeliveryReportMessage) o;
				try
				{
					responseObject = new JSONObject(response);
					try
					{
						@SuppressWarnings("unchecked")
						Iterator<String> iter = responseObject.keys();
						String messageId = iter.next();
						JSONObject statusObject = responseObject.getJSONObject(messageId);
						String status = statusObject.getString("status");
						if ((status.equalsIgnoreCase("q")) || (status.equalsIgnoreCase("r")) || (status.equalsIgnoreCase("a")) || (status.equalsIgnoreCase("b"))) message2.setDeliveryStatus(DeliveryStatus.Pending);
						if (status.equalsIgnoreCase("d")) message2.setDeliveryStatus(DeliveryStatus.Delivered);
						if ((status.equalsIgnoreCase("f")) || (status.equalsIgnoreCase("e")) || (status.equalsIgnoreCase("j"))) message2.setDeliveryStatus(DeliveryStatus.Failed);
						if (status.equalsIgnoreCase("u")) message2.setDeliveryStatus(DeliveryStatus.Unknown);
						if (statusObject.has("complete_time")) message2.setOriginalReceivedDate(new Date(statusObject.getLong("complete_time") * 1000));
					}
					catch (JSONException e)
					{
						logger.error("Error parsing response, wrong password?", e);
					}
				}
				catch (JSONException e)
				{
					logger.error("Error parsing response!", e);
					((AbstractGateway) o).getCreditBalance().setCredits(0);
				}
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
