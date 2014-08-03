
package org.smslib.smsserver.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.callback.IMessageSentCallback;
import org.smslib.callback.events.MessageSentCallbackEvent;
import org.smslib.smsserver.SMSServer;

public class MessageSentCallback implements IMessageSentCallback
{
	static Logger logger = LoggerFactory.getLogger(MessageSentCallback.class);

	@Override
	public boolean process(MessageSentCallbackEvent event)
	{
		try
		{
			SMSServer.getInstance().getDatabaseHandler().markMessageSent(event);
			return true;
		}
		catch (Exception e)
		{
			logger.error("Error!", e);
			return false;
		}
	}
}
