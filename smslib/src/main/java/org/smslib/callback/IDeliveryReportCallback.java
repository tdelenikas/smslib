
package org.smslib.callback;

import org.smslib.callback.events.DeliveryReportCallbackEvent;

public interface IDeliveryReportCallback
{
	public boolean process(DeliveryReportCallbackEvent event);
}
