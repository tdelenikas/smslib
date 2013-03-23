
package org.smslib.callback.events;

public class QueueThresholdCallbackEvent extends BaseCallbackEvent
{
	int queueLoad;

	public QueueThresholdCallbackEvent(int queueLoad)
	{
		this.queueLoad = queueLoad;
	}

	public int getQueueLoad()
	{
		return this.queueLoad;
	}
}
