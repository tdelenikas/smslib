
package org.smslib.callback.events;

import org.smslib.message.InboundMessage;

public class InboundMessageEvent extends BaseCallbackEvent
{
	InboundMessage message;

	public InboundMessageEvent(InboundMessage message)
	{
		this.message = message;
	}

	public InboundMessage getMessage()
	{
		return this.message;
	}
}
