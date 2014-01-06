
package org.smslib.message;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.ajwcc.pduUtils.gsm3040.SmsDeliveryPdu;
import org.smslib.Service;

public class InboundEncryptedMessage extends InboundBinaryMessage
{
	private static final long serialVersionUID = 1L;

	public InboundEncryptedMessage(SmsDeliveryPdu pdu, String memLocation, int memIndex)
	{
		super(pdu, memLocation, memIndex);
	}

	public byte[] getDecryptedData() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException
	{
		if (Service.getInstance().getKeyManager().getKey(getOriginatorAddress()) != null) return (Service.getInstance().getKeyManager().decrypt(getOriginatorAddress(), getPayload().getBytes()));
		return new byte[0];
	}
}
