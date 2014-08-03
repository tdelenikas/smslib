
package org.smslib.gateway.http.bulksms;

import java.net.URLConnection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import org.smslib.core.Capabilities;
import org.smslib.core.Capabilities.Caps;
import org.smslib.gateway.AbstractGateway;
import org.smslib.gateway.http.AbstractHttpGateway;
import org.smslib.message.DeliveryReportMessage;
import org.smslib.message.DeliveryReportMessage.DeliveryStatus;
import org.smslib.message.AbstractMessage.Encoding;
import org.smslib.message.OutboundMessage;
import org.smslib.message.OutboundMessage.FailureCause;
import org.smslib.message.OutboundMessage.SentStatus;

public class BulkSmsInternational extends AbstractHttpGateway
{
	String username;

	String password;

	public BulkSmsInternational(String gatewayId, String username, String password)
	{
		super(gatewayId, "BULKSMS International (http://www.bulksms.com/int/)");
		Capabilities caps = new Capabilities();
		caps.set(Caps.CanSendMessage);
		caps.set(Caps.CanSendFlashMessage);
		caps.set(Caps.CanSetSenderId);
		caps.set(Caps.CanSplitMessages);
		caps.set(Caps.CanQueryCreditBalance);
		caps.set(Caps.CanRequestDeliveryStatus);
		caps.set(Caps.CanQueryDeliveryStatus);
		setCapabilities(caps);
		this.operatorId = "bulksms-int";
		setBaseUrl("http://bulksms.vsms.net:5567");
		setSubmitMessageUrl("/eapi/submission/send_sms/2/2.0");
		setQueryMessageUrl("/eapi/status_reports/get_report/2/2.0");
		setQueryBalanceUrl("/eapi/user/get_credits/1/1.1");
		setHttpMethod(HttpMethod.POST);
		setHttpEncoding("ISO-8859-1");
		setMaxMessageParts(1);
		this.username = username;
		this.password = password;
	}

	public BulkSmsInternational(String gatewayId, String... parms)
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
				if (!message.getOriginatorAddress().isVoid()) parameters.put("sender", message.getOriginatorAddress().getAddress());
				else if (!getSenderAddress().isVoid()) parameters.put("sender", getSenderAddress().getAddress());
				parameters.put("username", this.username);
				parameters.put("password", this.password);
				parameters.put("msisdn", message.getRecipientAddress().getAddress());
				parameters.put("message", translateText(message.getPayload().getText()));
				if (message.getEncoding() == Encoding.EncUcs2) parameters.put("dca", "16bit");
				parameters.put("source_id", message.getId());
				if (message.isFlashSms()) parameters.put("msg_class", "0");
				if (message.getRequestDeliveryReport() || getRequestDeliveryReport()) parameters.put("want_report", "1");
				if (getMaxMessageParts() == 1) parameters.put("allow_concat_text_sms", "0");
				else
				{
					parameters.put("allow_concat_text_sms", "1");
					parameters.put("concat_text_sms_max_parts", Integer.toString(getMaxMessageParts()));
				}
				break;
			case QueryBalance:
				parameters.put("username", this.username);
				parameters.put("password", this.password);
				break;
			case QueryMessage:
				parameters.put("username", this.username);
				parameters.put("password", this.password);
				parameters.put("batch_id", ((DeliveryReportMessage) o).getOperatorMessageId());
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
		tokens = new StringTokenizer(responseList.get(0), "|");
		t1 = tokens.nextToken();
		t2 = (tokens.hasMoreTokens() ? tokens.nextToken().trim() : "");
		t3 = (tokens.hasMoreTokens() ? tokens.nextToken().trim() : "");
		switch (operation)
		{
			case SendMessage:
				message = (OutboundMessage) o;
				if (t1.equalsIgnoreCase("0"))
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
					message.setOperatorFailureCode(t1);
					switch (Integer.parseInt(t1))
					{
						case 22:
							message.setFailureCause(FailureCause.GatewayFailure);
							break;
						case 23:
							message.setFailureCause(FailureCause.AuthFailure);
							break;
						case 24:
							message.setFailureCause(FailureCause.BadFormat);
							break;
						case 25:
						case 26:
							message.setFailureCause(FailureCause.NoCredit);
							break;
						case 27:
						case 28:
							message.setFailureCause(FailureCause.OverQuota);
							break;
						case 40:
							message.setFailureCause(FailureCause.Unavailable);
							break;
						default:
							message.setFailureCause(FailureCause.UnknownFailure);
					}
				}
				break;
			case QueryBalance:
				if (t1.equalsIgnoreCase("0")) ((AbstractGateway) o).getCreditBalance().setCredits(Double.parseDouble(t2));
				break;
			case QueryMessage:
				DeliveryReportMessage message2 = (DeliveryReportMessage) o;
				if (t1.equalsIgnoreCase("0"))
				{
					tokens = new StringTokenizer(responseList.get(2), "|");
					t1 = tokens.nextToken();
					t2 = tokens.nextToken();
					switch (Integer.parseInt(t2))
					{
						case 11:
							message2.setDeliveryStatus(DeliveryStatus.Delivered);
							break;
						case 70:
							message2.setDeliveryStatus(DeliveryStatus.Unknown);
							break;
						case 53:
							message2.setDeliveryStatus(DeliveryStatus.Expired);
							break;
						case 0:
						case 10:
						case 63:
						case 64:
							message2.setDeliveryStatus(DeliveryStatus.Pending);
							break;
						default:
							message2.setDeliveryStatus(DeliveryStatus.Failed);
							break;
					}
				}
				else message2.setDeliveryStatus(DeliveryStatus.Error);
				break;
			default:
				throw new RuntimeException("Not implemented!");
		}
	}

	@Override
	protected String translateText(String text)
	{
		StringBuffer buffer = new StringBuffer(256);
		for (int i = 0; i < text.length(); i++)
		{
			switch (text.charAt(i))
			{
				case '^':
					buffer.append((char) 0xbb);
					buffer.append((char) 0x14);
					break;
				case '{':
					buffer.append((char) 0xbb);
					buffer.append((char) 0x28);
					break;
				case '}':
					buffer.append((char) 0xbb);
					buffer.append((char) 0x29);
					break;
				case '[':
					buffer.append((char) 0xbb);
					buffer.append((char) 0x3c);
					break;
				case ']':
					buffer.append((char) 0xbb);
					buffer.append((char) 0x3e);
					break;
				case '|':
					buffer.append((char) 0xbb);
					buffer.append((char) 0x40);
					break;
				case '\\':
					buffer.append((char) 0xbb);
					buffer.append((char) 0x2f);
					break;
				case '~':
					buffer.append((char) 0xbb);
					buffer.append((char) 0x3d);
					break;
				case '€':
					buffer.append((char) 0xbb);
					buffer.append((char) 0x65);
					break;
				case 'Δ':
					buffer.append((char) 0xd0);
					break;
				case 'Φ':
					buffer.append((char) 0xde);
					break;
				case 'Γ':
					buffer.append((char) 0xac);
					break;
				case 'Λ':
					buffer.append((char) 0xc2);
					break;
				case 'Ω':
					buffer.append((char) 0xdb);
					break;
				case 'Π':
					buffer.append((char) 0xba);
					break;
				case 'Ψ':
					buffer.append((char) 0xdd);
					break;
				case 'Σ':
					buffer.append((char) 0xca);
					break;
				case 'Θ':
					buffer.append((char) 0xd4);
					break;
				case 'Ξ':
					buffer.append((char) 0xb1);
					break;
				default:
					buffer.append(text.charAt(i));
			}
		}
		return buffer.toString();
	}
}
