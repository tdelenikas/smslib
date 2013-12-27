
package org.smslib.message;

import java.util.Date;
import org.ajwcc.pduUtils.gsm3040.PduUtils;
import org.ajwcc.pduUtils.gsm3040.SmsDeliveryPdu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InboundMessage extends AbstractMessage
{
	static Logger logger = LoggerFactory.getLogger(InboundMessage.class);

	private static final long serialVersionUID = 1L;

	String operatorMessageId;

	int memIndex;

	String memLocation;

	int mpRefNo;

	int mpMaxNo;

	int mpSeqNo;

	String mpMemIndex;

	String udh;

	String ud;

	MsIsdn smscNumber;

	boolean endsWithMultiChar;

	public InboundMessage(SmsDeliveryPdu pdu, String memLocation, int memIndex)
	{
		super(Type.Inbound, null, null, null);
		setMemLocation(memLocation);
		setMemIndex(memIndex);
		setMpRefNo(0);
		setMpMaxNo(0);
		setMpSeqNo(0);
		setMpMemIndex(-1);
		int dcsEncoding = PduUtils.extractDcsEncoding(pdu.getDataCodingScheme());
		switch (dcsEncoding)
		{
			case PduUtils.DCS_ENCODING_7BIT:
				setEncoding(Encoding.Enc7);
				break;
			case PduUtils.DCS_ENCODING_8BIT:
				setEncoding(Encoding.Enc8);
				break;
			case PduUtils.DCS_ENCODING_UCS2:
				setEncoding(Encoding.EncUcs2);
				break;
			default:
				logger.error("Unknown DCS Encoding: " + dcsEncoding);
		}
		setOriginatorAddress(new MsIsdn(pdu.getAddress()));
		setSentDate(pdu.getTimestamp());
		setSmscNumber(new MsIsdn(pdu.getSmscAddress()));
		setPayload(new Payload(pdu.getDecodedText()));
		if (pdu.isConcatMessage())
		{
			setMpRefNo(pdu.getMpRefNo());
			setMpMaxNo(pdu.getMpMaxNo());
			setMpSeqNo(pdu.getMpSeqNo());
		}
		if (pdu.isPortedMessage())
		{
			setSourcePort(pdu.getSrcPort());
			setDestinationPort(pdu.getDestPort());
		}
		if (pdu.hasTpUdhi())
		{
			this.udh = PduUtils.bytesToPdu(pdu.getUDHData());
		}
		this.ud = PduUtils.bytesToPdu(pdu.getUserDataAsBytes());
		if (getEncoding() == Encoding.Enc7)
		{
			byte[] temp = PduUtils.encodedSeptetsToUnencodedSeptets(pdu.getUDData());
			if (temp.length == 0) this.endsWithMultiChar = false;
			else if (temp[temp.length - 1] == 0x1b) this.endsWithMultiChar = true;
		}
	}

	public InboundMessage(String originator, String text, Date sentDate, String memLocation, int memIndex)
	{
		super(Type.Inbound, null, null, null);
		setMemLocation(memLocation);
		setMemIndex(memIndex);
		setOriginatorAddress(new MsIsdn(originator));
		setPayload(new Payload(text));
		setSentDate(sentDate);
	}

	public InboundMessage(Type type, String memLocation, int memIndex)
	{
		super(type, null, null, null);
		setOriginatorAddress(new MsIsdn());
		setMemIndex(memIndex);
		setMemLocation(memLocation);
		setMpRefNo(0);
		setMpMaxNo(0);
		setMpSeqNo(0);
		setMpMemIndex(-1);
		setSmscNumber(new MsIsdn());
	}

	public String getOperatorMessageId()
	{
		return this.operatorMessageId;
	}

	public void setOperatorMessageId(String operatorMessageId)
	{
		this.operatorMessageId = operatorMessageId;
	}

	public int getMemIndex()
	{
		return this.memIndex;
	}

	public void setMemIndex(int memIndex)
	{
		this.memIndex = memIndex;
	}

	public String getMemLocation()
	{
		return this.memLocation;
	}

	public void setMemLocation(String memLocation)
	{
		this.memLocation = memLocation;
	}

	public int getMpMaxNo()
	{
		return this.mpMaxNo;
	}

	public void setMpMaxNo(int myMpMaxNo)
	{
		this.mpMaxNo = myMpMaxNo;
	}

	public String getMpMemIndex()
	{
		return this.mpMemIndex;
	}

	public void setMpMemIndex(int myMpMemIndex)
	{
		if (myMpMemIndex == -1) this.mpMemIndex = "";
		else this.mpMemIndex += (this.mpMemIndex.length() == 0 ? "" : ",") + myMpMemIndex;
	}

	public int getMpRefNo()
	{
		return this.mpRefNo;
	}

	public void setMpRefNo(int myMpRefNo)
	{
		this.mpRefNo = myMpRefNo;
	}

	public int getMpSeqNo()
	{
		return this.mpSeqNo;
	}

	public void setMpSeqNo(int myMpSeqNo)
	{
		this.mpSeqNo = myMpSeqNo;
	}

	public String getUdh()
	{
		return this.udh;
	}

	public String getUd()
	{
		return this.ud;
	}

	public void setSmscNumber(MsIsdn smscNumber)
	{
		this.smscNumber = smscNumber;
	}

	public boolean getEndsWithMultiChar()
	{
		return this.endsWithMultiChar;
	}

	public void setEndsWithMultiChar(boolean b)
	{
		this.endsWithMultiChar = b;
	}

	@Override
	public String getSignature()
	{
		return hashSignature(String.format("%s-%s-%s", getOriginatorAddress(), getSentDate(), getPayload().getText()));
	}

	@Override
	public String toShortString()
	{
		return String.format("[%s @ %s]", getId(), getOriginatorAddress());
	}
}
