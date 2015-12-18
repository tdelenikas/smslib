
package org.smslib.message;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;
import org.smslib.helper.Common;
import org.smslib.message.OutboundMessage.SentStatus;

public abstract class AbstractMessage implements Serializable
{
	public enum Encoding
	{
		Enc7("7"), Enc8("8"), EncUcs2("U"), EncCustom("C");
		private final String shortString;

		private Encoding(String shortString)
		{
			this.shortString = shortString;
		}

		public String toShortString()
		{
			return this.shortString;
		}

		public static Encoding getEncodingFromShortString(String shortString)
		{
			if (shortString.equalsIgnoreCase("7")) return Enc7;
			else if (shortString.equalsIgnoreCase("8")) return Enc8;
			else if (shortString.equalsIgnoreCase("U")) return EncUcs2;
			else if (shortString.equalsIgnoreCase("C")) return EncCustom;
			else return null;
		}
	}

	public enum DcsClass
	{
		None, Flash, Me, Sim, Te
	}

	public enum Type
	{
		Inbound, Outbound, StatusReport
	}

	private static final long serialVersionUID = 1L;

	Date creationDate = new Date();

	String id = UUID.randomUUID().toString();

	MsIsdn originatorAddress = new MsIsdn();

	MsIsdn recipientAddress = new MsIsdn();

	Payload payload = new Payload("");

	Type type = Type.Inbound;

	Encoding encoding = Encoding.Enc7;

	DcsClass dcsClass = DcsClass.Sim;

	String gatewayId = "";

	int sourcePort = -1;

	int destinationPort = -1;

	Date sentDate = null;

	public AbstractMessage()
	{
	}

	public AbstractMessage(AbstractMessage m)
	{
		this.creationDate = m.getCreationDate();
		this.originatorAddress = new MsIsdn(m.getOriginatorAddress());
		this.recipientAddress = new MsIsdn(m.getRecipientAddress());
		this.payload = new Payload(m.getPayload());
		this.type = m.getType();
		this.encoding = m.getEncoding();
		this.dcsClass = m.getDcsClass();
		this.gatewayId = m.getGatewayId();
		this.sourcePort = m.getSourcePort();
		this.destinationPort = m.getDestinationPort();
		this.sentDate = m.getSentDate();
	}

	public AbstractMessage(Type type, MsIsdn originatorAddress, MsIsdn recipientAddress, Payload payload)
	{
		setType(type);
		setOriginatorAddress(originatorAddress);
		setRecipientAddress(recipientAddress);
		setPayload(payload);
	}

	public String getId()
	{
		return this.id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public Date getCreationDate()
	{
		return this.creationDate;
	}

	public MsIsdn getOriginatorAddress()
	{
		return this.originatorAddress;
	}

	public void setOriginatorAddress(MsIsdn originator)
	{
		this.originatorAddress = originator;
	}

	public MsIsdn getRecipientAddress()
	{
		return this.recipientAddress;
	}

	public void setRecipientAddress(MsIsdn recipient)
	{
		this.recipientAddress = recipient;
	}

	public Payload getPayload()
	{
		return this.payload;
	}

	public void setPayload(Payload payload)
	{
		this.payload = payload;
	}

	public Type getType()
	{
		return this.type;
	}

	public void setType(Type type)
	{
		this.type = type;
	}

	public Encoding getEncoding()
	{
		return this.encoding;
	}

	public void setEncoding(Encoding encoding)
	{
		this.encoding = encoding;
	}

	public DcsClass getDcsClass()
	{
		return this.dcsClass;
	}

	public void setDcsClass(DcsClass dcsClass)
	{
		this.dcsClass = dcsClass;
	}

	public String getGatewayId()
	{
		return this.gatewayId;
	}

	public void setGatewayId(String gatewayId)
	{
		this.gatewayId = gatewayId;
	}

	public int getSourcePort()
	{
		return this.sourcePort;
	}

	public void setSourcePort(int sourcePort)
	{
		this.sourcePort = sourcePort;
	}

	public int getDestinationPort()
	{
		return this.destinationPort;
	}

	public void setDestinationPort(int destinationPort)
	{
		this.destinationPort = destinationPort;
	}

	public Date getSentDate()
	{
		return (this.sentDate != null ? (Date) this.sentDate.clone() : null);
	}

	public void setSentDate(Date sentDate)
	{
		this.sentDate = new Date(sentDate.getTime());
	}

	public abstract String getSignature();

	public abstract String toShortString();

	public String hashSignature(String s)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(s.getBytes(), 0, s.length());
			BigInteger i = new BigInteger(1, md.digest());
			return String.format("%1$032x", i);
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString()
	{
		StringBuffer b = new StringBuffer(1024);
		b.append(String.format("%n== MESSAGE START ======================================================================%n"));
		b.append(String.format("CLASS: %s%n", this.getClass().toString()));
		b.append(String.format("Message ID: %s%n", getId()));
		b.append(String.format("Message Signature: %s%n", getSignature()));
		b.append(String.format("Via Gateway: %s%n", getGatewayId() == null ? "N/A" : getGatewayId()));
		b.append(String.format("Creation Date: %s%n", getCreationDate()));
		b.append(String.format("Type: %s%n", getType()));
		b.append(String.format("Encoding: %s%n", getEncoding()));
		b.append(String.format("DCS Class: %s%n", getDcsClass()));
		b.append(String.format("Source Port: %s%n", getSourcePort()));
		b.append(String.format("Destination Port: %s%n", getDestinationPort()));
		b.append(String.format("Originator Address: %s%n", getOriginatorAddress()));
		b.append(String.format("Recipient Address: %s%n", getRecipientAddress()));
		if (getPayload() != null)
		{
			b.append(String.format("Payload Type: %s%n", getPayload().getType()));
			b.append(String.format("Text payload: %s%n", (getPayload().getText() == null ? "null" : getPayload().getText())));
			b.append(String.format("Binary payload: %s%n", (getPayload().getBytes() == null ? "null" : Common.bytesToString(getPayload().getBytes()))));
		}
		if (this instanceof InboundMessage)
		{
			b.append(String.format("Sent Date: %s%n", getSentDate()));
			b.append(String.format("Memory Storage Location: %s%n", ((InboundMessage) this).getMemLocation()));
			b.append(String.format("Memory Index: %d%n", ((InboundMessage) this).getMemIndex()));
			b.append(String.format("Memory MP Index: %s%n", ((InboundMessage) this).getMpMemIndex()));
		}
		if (this instanceof InboundEncryptedMessage)
		{
			try
			{
				b.append(String.format("Decrypted binary payload: %s%n", Common.bytesToString(((InboundEncryptedMessage) this).getDecryptedData())));
			}
			catch (Exception e)
			{
				b.append(String.format("Cannot decrypt, due to %s.", e.getMessage()));
			}
		}
		if (this instanceof OutboundMessage)
		{
			b.append(String.format("Sent Date: %s%n", (((OutboundMessage) this).getSentStatus() == SentStatus.Sent ? getSentDate() : "N/A")));
			String ids = "";
			for (String opId : ((OutboundMessage) this).getOperatorMessageIds())
			{
				ids += (ids.length() == 0 ? opId : "," + opId);
			}
			b.append(String.format("Operator Message IDs: %s%n", ids));
			b.append(String.format("Status: %s%n", ((OutboundMessage) this).getSentStatus().toString()));
			b.append(String.format("Credits used: %f%n", ((OutboundMessage) this).getCreditsUsed()));
			b.append(String.format("Failure: %s%n", ((OutboundMessage) this).getFailureCause().toString()));
			b.append(String.format("Operator Failure Code: %s%n", ((OutboundMessage) this).getOperatorFailureCode()));
			b.append(String.format("Request Delivery Reports: %b%n", ((OutboundMessage) this).getRequestDeliveryReport()));
		}
		if (this instanceof DeliveryReportMessage)
		{
			b.append(String.format("Original Operator Message Id: %s%n", ((DeliveryReportMessage) this).getOriginalOperatorMessageId()));
			b.append(String.format("Delivery Date: %s%n", ((DeliveryReportMessage) this).getOriginalReceivedDate()));
			b.append(String.format("Delivery Status: %s%n", ((DeliveryReportMessage) this).getDeliveryStatus()));
		}
		b.append(String.format("== MESSAGE END ========================================================================%n"));
		return b.toString();
	}
}
