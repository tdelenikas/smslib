
package org.smslib.message;

public class Payload
{
	public enum Type
	{
		Text, Binary
	}

	private String textData;

	private byte[] binaryData;

	private Type type;

	public Payload(String data)
	{
		this.type = Type.Text;
		this.textData = data;
	}

	public Payload(byte[] data)
	{
		this.type = Type.Binary;
		this.binaryData = data.clone();
	}

	public Payload(Payload p)
	{
		this.type = p.getType();
		this.textData = (this.type == Type.Text ? p.getText() : "");
		this.binaryData = (this.type == Type.Binary ? p.getBytes().clone() : null);
	}

	public Type getType()
	{
		return this.type;
	}

	public String getText()
	{
		return (this.type == Type.Text ? this.textData : null);
	}

	public byte[] getBytes()
	{
		return (this.type == Type.Binary ? this.binaryData : null);
	}

	public boolean isMultipart()
	{
		return false;
	}
}
