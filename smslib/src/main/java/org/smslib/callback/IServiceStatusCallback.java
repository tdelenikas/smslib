
package org.smslib.callback;

import org.smslib.callback.events.ServiceStatusCallbackEvent;

public interface IServiceStatusCallback
{
	public boolean process(ServiceStatusCallbackEvent event);
}
