
package org.smslib.callback.events;

import org.smslib.message.DeliveryReportMessage;

public class DeliveryReportCallbackEvent extends BaseCallbackEvent
{
	DeliveryReportMessage message;

	public DeliveryReportCallbackEvent(DeliveryReportMessage message)
	{
		this.message = message;
	}

	public DeliveryReportMessage getMessage()
	{
		return this.message;
	}
}
