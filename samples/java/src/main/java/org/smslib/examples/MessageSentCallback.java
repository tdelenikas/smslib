package org.smslib.examples;

import org.smslib.callback.IMessageSentCallback;
import org.smslib.callback.events.MessageSentCallbackEvent;
import org.smslib.helper.Log;

public class MessageSentCallback implements IMessageSentCallback
{
	@Override
	public boolean process(MessageSentCallbackEvent event)
	{
		Log.getInstance().getLog().info("[MessageSentCallback] " + event.getMessage().toShortString());
		return true;
	}
}
