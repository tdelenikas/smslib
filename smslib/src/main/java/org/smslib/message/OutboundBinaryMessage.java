
package org.smslib.message;

public class OutboundBinaryMessage extends OutboundMessage
{
	private static final long serialVersionUID = 1L;

	public OutboundBinaryMessage()
	{
	}

	public OutboundBinaryMessage(MsIsdn originator, MsIsdn recipient, byte[] data)
	{
		super(originator, recipient, new Payload(data));
		setEncoding(Encoding.Enc8);
	}

	public OutboundBinaryMessage(MsIsdn recipient, byte[] data)
	{
		this(new MsIsdn(""), recipient, data);
	}

	public OutboundBinaryMessage(String originator, String recipient, byte[] data)
	{
		this(new MsIsdn(originator), new MsIsdn(recipient), data);
	}

	public OutboundBinaryMessage(String recipient, byte[] data)
	{
		this(new MsIsdn(""), new MsIsdn(recipient), data);
	}
}
