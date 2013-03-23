
package org.smslib.crypto;

public abstract class AbstractKey
{
	public static String asHex(byte buf[])
	{
		StringBuffer strbuf = new StringBuffer(buf.length * 2);
		int i;
		for (i = 0; i < buf.length; i++)
		{
			if ((buf[i] & 0xff) < 0x10) strbuf.append("0");
			strbuf.append(Long.toString(buf[i] & 0xff, 16));
		}
		return strbuf.toString();
	}

	public static String asString(byte[] bytes)
	{
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < bytes.length; i++)
			buffer.append((char) bytes[i]);
		return buffer.toString();
	}
}
