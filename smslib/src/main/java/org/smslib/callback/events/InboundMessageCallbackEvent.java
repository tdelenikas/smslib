
package org.smslib.callback.events;

import org.smslib.message.InboundMessage;

public class InboundMessageCallbackEvent extends BaseCallbackEvent
{
	InboundMessage message;

	public InboundMessageCallbackEvent(InboundMessage message)
	{
		this.message = message;
	}

	public InboundMessage getMessage()
	{
		return this.message;
	}
}
