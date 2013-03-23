
package org.smslib.callback.events;

import org.smslib.message.OutboundMessage;

public class MessageSentCallbackEvent extends BaseCallbackEvent
{
	OutboundMessage message;

	public MessageSentCallbackEvent(OutboundMessage message)
	{
		this.message = message;
	}

	public OutboundMessage getMessage()
	{
		return this.message;
	}
}
