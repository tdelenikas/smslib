
package org.smslib.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.callback.IMessageSentCallback;
import org.smslib.callback.events.MessageSentCallbackEvent;

public class MessageSentCallback implements IMessageSentCallback
{
	final Logger logger = LoggerFactory.getLogger(MessageSentCallback.class);

	@Override
	public boolean process(MessageSentCallbackEvent event)
	{
		logger.info("[MessageSentCallback] " + event.getMessage().toShortString());
		return true;
	}
}
