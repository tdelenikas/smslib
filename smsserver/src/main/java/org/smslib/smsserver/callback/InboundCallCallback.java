
package org.smslib.smsserver.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.callback.IInboundCallCallback;
import org.smslib.callback.events.InboundCallEvent;
import org.smslib.smsserver.SMSServer;
import org.smslib.smsserver.db.data.InboundCallDefinition;

public class InboundCallCallback implements IInboundCallCallback
{
	static Logger logger = LoggerFactory.getLogger(InboundCallCallback.class);

	@Override
	public boolean process(InboundCallEvent event)
	{
		try
		{
			SMSServer.getInstance().getDatabaseHandler().SaveInboundCall(new InboundCallDefinition(event.getDate(), event.getMsisdn(), event.getGatewayId()));
			return true;
		}
		catch (Exception e)
		{
			logger.error("Error!", e);
			return false;
		}
	}
}
