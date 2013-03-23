
package org.smslib.callback;

import org.smslib.callback.events.MessageSentCallbackEvent;

public interface IMessageSentCallback
{
	public boolean process(MessageSentCallbackEvent event);
}
