
package org.smslib.message;

import org.ajwcc.pduUtils.gsm3040.SmsDeliveryPdu;

public class InboundBinaryMessage extends InboundMessage
{
	private static final long serialVersionUID = 1L;

	public InboundBinaryMessage(SmsDeliveryPdu pdu, String memLocation, int memIndex)
	{
		super(pdu, memLocation, memIndex);
		setPayload(new Payload(pdu.getUserDataAsBytes()));
	}
}
