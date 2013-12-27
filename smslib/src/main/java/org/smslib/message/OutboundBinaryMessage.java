
package org.smslib.message;

public class OutboundBinaryMessage extends OutboundMessage
{
	private static final long serialVersionUID = 1L;

	public OutboundBinaryMessage()
	{
	}

	public OutboundBinaryMessage(MsIsdn originatorAddress, MsIsdn recipientAddress, byte[] data)
	{
		super(originatorAddress, recipientAddress, new Payload(data));
		setEncoding(Encoding.Enc8);
	}

	public OutboundBinaryMessage(MsIsdn recipient, byte[] data)
	{
		this(new MsIsdn(""), recipient, data);
	}

	public OutboundBinaryMessage(String originatorAddress, String recipientAddress, byte[] data)
	{
		this(new MsIsdn(originatorAddress), new MsIsdn(recipientAddress), data);
	}

	public OutboundBinaryMessage(String recipientAddress, byte[] data)
	{
		this(new MsIsdn(""), new MsIsdn(recipientAddress), data);
	}
}
