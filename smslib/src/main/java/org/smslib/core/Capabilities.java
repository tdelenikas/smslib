
package org.smslib.core;

import java.util.BitSet;
import org.smslib.message.AbstractMessage.Encoding;
import org.smslib.message.OutboundBinaryMessage;
import org.smslib.message.OutboundMessage;
import org.smslib.message.OutboundWapMessage;

public class Capabilities
{
	BitSet caps = new BitSet();

	public enum Caps
	{
		CanSendMessage, CanSendBinaryMessage, CanSendUnicodeMessage, CanSendWapMessage, CanSendFlashMessage, CanSendPortInfo, CanSetSenderId, CanSplitMessages, CanRequestDeliveryStatus, CanQueryDeliveryStatus, CanQueryCreditBalance, CanQueryCoverage, CanSetValidityPeriod
	}

	public Capabilities()
	{
	}

	public void set(Caps c)
	{
		this.caps.set(c.ordinal());
	}

	public void clear(Caps c)
	{
		this.caps.clear(c.ordinal());
	}

	public BitSet getCapabilities()
	{
		return (BitSet) this.caps.clone();
	}

	public boolean matches(OutboundMessage message)
	{
		BitSet bs = new BitSet();
		bs.set(Caps.CanSendMessage.ordinal());
		for (Caps c : Caps.values())
		{
			if (c == Caps.CanSetSenderId)
			{
				if (message.getOriginatorAddress().getAddress().length() > 0) bs.set(c.ordinal());
			}
			else if (c == Caps.CanRequestDeliveryStatus)
			{
				if (message.getRequestDeliveryReport()) bs.set(c.ordinal());
			}
			else if (c == Caps.CanSplitMessages)
			{
				if (message.getPayload().isMultipart()) bs.set(c.ordinal());
			}
			else if (c == Caps.CanSetValidityPeriod)
			{
				if (message.getValidityPeriod() != 0) bs.set(c.ordinal());
			}
			else if (c == Caps.CanSendFlashMessage)
			{
				if (message.isFlashSms()) bs.set(c.ordinal());
			}
			else if (c == Caps.CanSendUnicodeMessage)
			{
				if (message.getEncoding() == Encoding.EncUcs2) bs.set(c.ordinal());
			}
			else if (c == Caps.CanSendBinaryMessage)
			{
				if ((message instanceof OutboundBinaryMessage) || (message.getEncoding() == Encoding.Enc8)) bs.set(c.ordinal());
			}
			else if (c == Caps.CanSendUnicodeMessage)
			{
				if (message.getEncoding() == Encoding.EncUcs2) bs.set(c.ordinal());
			}
			else if (c == Caps.CanSendWapMessage)
			{
				if (message instanceof OutboundWapMessage) bs.set(c.ordinal());
			}
			else if (c == Caps.CanSendPortInfo)
			{
				if (message.getSourcePort() != -1 || message.getDestinationPort() != -1) bs.set(c.ordinal());
			}
		}
		BitSet originalBs = (BitSet) getCapabilities().clone();
		originalBs.and(bs);
		return (originalBs.cardinality() == bs.cardinality());
	}

	public boolean supports(Caps c)
	{
		BitSet bs = new BitSet();
		bs.set(c.ordinal());
		BitSet originalBs = (BitSet) getCapabilities().clone();
		originalBs.and(bs);
		return (originalBs.cardinality() == bs.cardinality());
	}

	@Override
	public String toString()
	{
		BitSet bs = (BitSet) getCapabilities().clone();
		StringBuffer b = new StringBuffer();
		for (Caps c : Caps.values())
		{
			b.append(String.format("%-30s : ", c.toString()));
			b.append(bs.get(c.ordinal()) ? "YES" : "NO");
			b.append("\n");
		}
		return b.toString();
	}
}
