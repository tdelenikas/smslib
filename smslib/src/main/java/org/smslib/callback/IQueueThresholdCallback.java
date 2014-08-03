
package org.smslib.callback;

import org.smslib.callback.events.QueueThresholdCallbackEvent;

public interface IQueueThresholdCallback
{
	public boolean process(QueueThresholdCallbackEvent event);
}
