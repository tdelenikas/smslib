
package org.smslib.smsserver.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.callback.IDeliveryReportCallback;
import org.smslib.callback.events.DeliveryReportCallbackEvent;
import org.smslib.smsserver.SMSServer;
import org.smslib.smsserver.db.data.DeliveryReportDefinition;

public class DeliveryReportCallback implements IDeliveryReportCallback
{
	static Logger logger = LoggerFactory.getLogger(DeliveryReportCallback.class);

	@Override
	public boolean process(DeliveryReportCallbackEvent event)
	{
		try
		{
			SMSServer.getInstance().getDatabaseHandler().SaveDeliveryReport(new DeliveryReportDefinition(event.getMessage().getDeliveryStatus().toShortString(), event.getMessage().getOriginalReceivedDate(), event.getMessage().getRecipientAddress(), event.getMessage().getOriginalOperatorMessageId(), event.getMessage().getGatewayId()));
			return true;
		}
		catch (Exception e)
		{
			logger.error("Error!", e);
			return false;
		}
	}
}
