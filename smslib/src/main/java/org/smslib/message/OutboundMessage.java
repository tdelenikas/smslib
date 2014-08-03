
package org.smslib.message;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.ajwcc.pduUtils.gsm3040.Pdu;
import org.ajwcc.pduUtils.gsm3040.PduFactory;
import org.ajwcc.pduUtils.gsm3040.PduGenerator;
import org.ajwcc.pduUtils.gsm3040.PduParser;
import org.ajwcc.pduUtils.gsm3040.PduUtils;
import org.ajwcc.pduUtils.gsm3040.SmsSubmitPdu;
import org.ajwcc.pduUtils.gsm3040.ie.InformationElementFactory;
import org.smslib.gateway.AbstractGateway;

public class OutboundMessage extends AbstractMessage
{
	private static final long serialVersionUID = 1L;

	public enum SentStatus
	{
		Sent("S"), Unsent("U"), Queued("Q"), Failed("F");
		private final String shortString;

		private SentStatus(String shortString)
		{
			this.shortString = shortString;
		}

		public String toShortString()
		{
			return this.shortString;
		}
	}

	public enum FailureCause
	{
		None("00"), BadNumber("01"), BadFormat("02"), GatewayFailure("03"), AuthFailure("04"), NoCredit("05"), OverQuota("06"), NoRoute("07"), Unavailable("08"), HttpError("09"), UnknownFailure("10"), Cancelled("11"), NoService("12"), MissingParms("13");
		private final String shortString;

		private FailureCause(String shortString)
		{
			this.shortString = shortString;
		}

		public String toShortString()
		{
			return this.shortString;
		}
	}

	SentStatus sentStatus = SentStatus.Unsent;

	double creditsUsed = 0;

	FailureCause failureCause = FailureCause.None;

	String operatorFailureCode = "";

	List<String> operatorMessageIds = new ArrayList<>();

	boolean requestDeliveryReport = false;

	int validityPeriod = 0;

	boolean flashSms = false;

	int priority = 0;

	LinkedList<AbstractGateway> routingTable = new LinkedList<>();

	public OutboundMessage()
	{
	}

	public OutboundMessage(MsIsdn originatorAddress, MsIsdn recipientAddress, Payload payload)
	{
		super(Type.Outbound, originatorAddress, recipientAddress, payload);
	}

	public OutboundMessage(MsIsdn originatorAddress, MsIsdn recipientAddress, String text)
	{
		this(originatorAddress, recipientAddress, new Payload(text));
	}

	public OutboundMessage(String originatorAddress, String recipientAddress, String text)
	{
		this(new MsIsdn(originatorAddress), new MsIsdn(recipientAddress), new Payload(text));
	}

	public OutboundMessage(MsIsdn recipientAddress, String text)
	{
		this(new MsIsdn(""), recipientAddress, new Payload(text));
	}

	public OutboundMessage(String recipientAddress, String text)
	{
		this(new MsIsdn(""), new MsIsdn(recipientAddress), new Payload(text));
	}

	public OutboundMessage(OutboundMessage m)
	{
		super(m);
		this.sentStatus = m.getSentStatus();
		this.creditsUsed = m.getCreditsUsed();
		this.failureCause = m.getFailureCause();
		this.operatorFailureCode = m.getOperatorFailureCode();
		this.requestDeliveryReport = m.getRequestDeliveryReport();
		this.validityPeriod = m.getValidityPeriod();
		this.flashSms = m.isFlashSms();
		this.priority = m.getPriority();
	}

	public SentStatus getSentStatus()
	{
		return this.sentStatus;
	}

	public void setSentStatus(SentStatus sentStatus)
	{
		this.sentStatus = sentStatus;
	}

	public double getCreditsUsed()
	{
		return this.creditsUsed;
	}

	public void setCreditsUsed(double creditsUsed)
	{
		this.creditsUsed = creditsUsed;
	}

	public FailureCause getFailureCause()
	{
		return this.failureCause;
	}

	public void setFailureCause(FailureCause failureCode)
	{
		this.failureCause = failureCode;
	}

	public String getOperatorFailureCode()
	{
		return this.operatorFailureCode;
	}

	public void setOperatorFailureCode(String failureCode)
	{
		this.operatorFailureCode = failureCode;
	}

	public String getOperatorMessageId()
	{
		return this.operatorMessageIds.get(0);
	}

	public List<String> getOperatorMessageIds()
	{
		return this.operatorMessageIds;
	}

	public boolean getRequestDeliveryReport()
	{
		return this.requestDeliveryReport;
	}

	public void setRequestDeliveryReport(boolean requestDeliveryReport)
	{
		this.requestDeliveryReport = requestDeliveryReport;
	}

	public int getValidityPeriod()
	{
		return this.validityPeriod;
	}

	public void setValidityPeriod(int validityPeriod)
	{
		this.validityPeriod = validityPeriod;
	}

	public int getPriority()
	{
		return this.priority;
	}

	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	public boolean isFlashSms()
	{
		return this.flashSms;
	}

	public void setFlashSms(boolean flashSms)
	{
		this.flashSms = flashSms;
		setDcsClass(this.flashSms ? DcsClass.Flash : DcsClass.None);
	}

	public LinkedList<AbstractGateway> getRoutingTable()
	{
		return this.routingTable;
	}

	public void setRoutingTable(LinkedList<AbstractGateway> routingTable)
	{
		this.routingTable = routingTable;
	}

	@Override
	public String toShortString()
	{
		return String.format("[%s @ %s]", getId(), getRecipientAddress());
	}

	public List<String> getPdus(MsIsdn smscNumber, int mpRefNo, boolean extRequestDeliveryReport)
	{
		PduGenerator pduGenerator = new PduGenerator();
		SmsSubmitPdu pdu = createPduObject(getRequestDeliveryReport() || extRequestDeliveryReport);
		initPduObject(pdu, smscNumber);
		return pduGenerator.generatePduList(pdu, mpRefNo);
	}

	protected SmsSubmitPdu createPduObject(boolean extRequestDeliveryReport)
	{
		return (extRequestDeliveryReport ? PduFactory.newSmsSubmitPdu(PduUtils.TP_SRR_REPORT | PduUtils.TP_VPF_INTEGER) : PduFactory.newSmsSubmitPdu());
	}

	protected void initPduObject(SmsSubmitPdu pdu, MsIsdn smscNumber)
	{
		if ((getSourcePort() > -1) && (getDestinationPort() > -1)) pdu.addInformationElement(InformationElementFactory.generatePortInfo(getDestinationPort(), getSourcePort()));
		String smscNumberForLengthCheck = smscNumber.getAddress();
		pdu.setSmscInfoLength(1 + (smscNumberForLengthCheck.length() / 2) + ((smscNumberForLengthCheck.length() % 2 == 1) ? 1 : 0));
		pdu.setSmscAddress(smscNumber.getAddress());
		pdu.setSmscAddressType(PduUtils.getAddressTypeFor(smscNumber));
		pdu.setMessageReference(0);
		pdu.setAddress(getRecipientAddress());
		pdu.setAddressType(PduUtils.getAddressTypeFor(getRecipientAddress()));
		pdu.setProtocolIdentifier(0);
		if (!pdu.isBinary())
		{
			int dcs = 0;
			if (getEncoding() == Encoding.Enc7) dcs = PduUtils.DCS_ENCODING_7BIT;
			else if (getEncoding() == Encoding.Enc8) dcs = PduUtils.DCS_ENCODING_8BIT;
			else if (getEncoding() == Encoding.EncUcs2) dcs = PduUtils.DCS_ENCODING_UCS2;
			else if (getEncoding() == Encoding.EncCustom) dcs = PduUtils.DCS_ENCODING_7BIT;
			if (getDcsClass() == DcsClass.Flash) dcs = dcs | PduUtils.DCS_MESSAGE_CLASS_FLASH;
			else if (getDcsClass() == DcsClass.Me) dcs = dcs | PduUtils.DCS_MESSAGE_CLASS_ME;
			else if (getDcsClass() == DcsClass.Sim) dcs = dcs | PduUtils.DCS_MESSAGE_CLASS_SIM;
			else if (getDcsClass() == DcsClass.Te) dcs = dcs | PduUtils.DCS_MESSAGE_CLASS_TE;
			pdu.setDataCodingScheme(dcs);
		}
		pdu.setValidityPeriod(getValidityPeriod());
		if (getEncoding() == Encoding.Enc8) pdu.setDataBytes(getPayload().getBytes());
		else pdu.setDecodedText(getPayload().getText());
	}

	public String getPduUserData(boolean extRequestDeliveryReport)
	{
		// generate
		PduGenerator pduGenerator = new PduGenerator();
		SmsSubmitPdu pdu = createPduObject(extRequestDeliveryReport);
		initPduObject(pdu, new MsIsdn());
		// NOTE: - the mpRefNo is arbitrarily set to 1
		// - this won't matter since we aren't looking at the UDH in this method
		// - this method is not allowed for 7-bit messages with UDH
		// since it is probable that the returned value will not be
		// correct due to the encoding's dependence on the UDH
		// - if the user wishes to extract the UD per part, he would need to get all pduStrings
		// using getPdus(String smscNumber, int mpRefNo), use a
		// PduParser on each pduString in the returned list, then access the UD via the Pdu object
		List<String> pdus = pduGenerator.generatePduList(pdu, 1);
		// my this point, pdu will be updated with concat info (in udhi), if present
		if ((pdu.hasTpUdhi()) && (getEncoding() == Encoding.Enc7)) { throw new RuntimeException("getPduUserData() not supported for 7-bit messages with UDH"); }
		// sum up the ud parts
		StringBuffer ud = new StringBuffer();
		for (String pduString : pdus)
		{
			Pdu newPdu = new PduParser().parsePdu(pduString);
			ud.append(PduUtils.bytesToPdu(newPdu.getUserDataAsBytes()));
		}
		return ud.toString();
	}

	public String getPduUserDataHeader(boolean extRequestDeliveryReport)
	{
		// generate
		PduGenerator pduGenerator = new PduGenerator();
		SmsSubmitPdu pdu = createPduObject(extRequestDeliveryReport);
		initPduObject(pdu, new MsIsdn());
		// NOTE: - the mpRefNo is arbitrarily set to 1
		// - if the user wishes to extract the UDH per part, he would need to get all pduStrings
		// using getPdus(String smscNumber, int mpRefNo), use a
		// PduParser on each pduString in the returned list, then access the UDH via the Pdu object
		List<String> pdus = pduGenerator.generatePduList(pdu, 1);
		Pdu newPdu = new PduParser().parsePdu(pdus.get(0));
		byte[] udh = newPdu.getUDHData();
		if (udh != null) return PduUtils.bytesToPdu(udh);
		return null;
	}

	@Override
	public String getSignature()
	{
		return hashSignature(String.format("%s-%s", getRecipientAddress(), getId()));
	}
}
