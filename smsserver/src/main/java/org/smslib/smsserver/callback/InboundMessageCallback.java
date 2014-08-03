
package org.smslib.smsserver.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.callback.IInboundMessageCallback;
import org.smslib.callback.events.InboundMessageCallbackEvent;
import org.smslib.smsserver.SMSServer;

public class InboundMessageCallback implements IInboundMessageCallback
{
	static Logger logger = LoggerFactory.getLogger(InboundMessageCallback.class);

	@Override
	public boolean process(InboundMessageCallbackEvent event)
	{
		try
		{
			SMSServer.getInstance().getDatabaseHandler().saveInboundMessage(event);
			return true;
		}
		catch (Exception e)
		{
			logger.error("Error!", e);
			return false;
		}
	}
}
