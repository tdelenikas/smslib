
package org.smslib.smsserver.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.callback.IInboundCallCallback;
import org.smslib.callback.events.InboundCallCallbackEvent;
import org.smslib.smsserver.SMSServer;

public class InboundCallCallback implements IInboundCallCallback
{
	static Logger logger = LoggerFactory.getLogger(InboundCallCallback.class);

	@Override
	public boolean process(InboundCallCallbackEvent event)
	{
		try
		{
			SMSServer.getInstance().getDatabaseHandler().saveInboundCall(event);
			return true;
		}
		catch (Exception e)
		{
			logger.error("Error!", e);
			return false;
		}
	}
}
