
package org.smslib.callback;

import org.smslib.callback.events.DequeueMessageCallbackEvent;

public interface IDequeueMessageCallback
{
	public boolean process(DequeueMessageCallbackEvent event);
}
