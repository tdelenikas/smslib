
package org.smslib.callback.events;

import org.smslib.message.OutboundMessage;

public class DequeueMessageCallbackEvent extends BaseCallbackEvent
{
	OutboundMessage message;

	public DequeueMessageCallbackEvent(OutboundMessage message)
	{
		this.message = message;
	}

	public OutboundMessage getMessage()
	{
		return this.message;
	}
}
