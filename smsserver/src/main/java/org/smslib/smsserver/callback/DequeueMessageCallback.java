
package org.smslib.smsserver.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.callback.IDequeueMessageCallback;
import org.smslib.callback.events.DequeueMessageCallbackEvent;
import org.smslib.message.OutboundMessage;
import org.smslib.smsserver.SMSServer;

public class DequeueMessageCallback implements IDequeueMessageCallback
{
	static Logger logger = LoggerFactory.getLogger(DequeueMessageCallback.class);

	@Override
	public boolean process(DequeueMessageCallbackEvent event)
	{
		try
		{
			SMSServer.getInstance().getDatabaseHandler().setMessageStatus(event.getMessage(), OutboundMessage.SentStatus.Unsent);
			return true;
		}
		catch (Exception e)
		{
			logger.error("Error!", e);
			return false;
		}
	}
}
