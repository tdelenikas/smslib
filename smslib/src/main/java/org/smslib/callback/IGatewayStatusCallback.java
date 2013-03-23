
package org.smslib.callback;

import org.smslib.callback.events.GatewayStatusCallbackEvent;

public interface IGatewayStatusCallback
{
	public boolean process(GatewayStatusCallbackEvent event);
}
