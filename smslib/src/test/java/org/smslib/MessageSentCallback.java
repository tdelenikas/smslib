
package org.smslib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.callback.IMessageSentCallback;
import org.smslib.callback.events.MessageSentCallbackEvent;

public class MessageSentCallback implements IMessageSentCallback
{
	static Logger logger = LoggerFactory.getLogger(MessageSentCallback.class);

	@Override
	public boolean process(MessageSentCallbackEvent event)
	{
		logger.info("[MessageSentCallback] " + event.getMessage().getId() + "/" + event.getMessage().getSentStatus() + "/" + event.getMessage().getFailureCause() + "/" + event.getMessage().getGatewayId());
		return true;
	}
}
