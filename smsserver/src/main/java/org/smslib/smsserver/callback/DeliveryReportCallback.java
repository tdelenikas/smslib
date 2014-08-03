
package org.smslib.smsserver.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.callback.IDeliveryReportCallback;
import org.smslib.callback.events.DeliveryReportCallbackEvent;
import org.smslib.smsserver.SMSServer;

public class DeliveryReportCallback implements IDeliveryReportCallback
{
	static Logger logger = LoggerFactory.getLogger(DeliveryReportCallback.class);

	@Override
	public boolean process(DeliveryReportCallbackEvent event)
	{
		try
		{
			SMSServer.getInstance().getDatabaseHandler().SaveDeliveryReport(event);
			return true;
		}
		catch (Exception e)
		{
			logger.error("Error!", e);
			return false;
		}
	}
}
