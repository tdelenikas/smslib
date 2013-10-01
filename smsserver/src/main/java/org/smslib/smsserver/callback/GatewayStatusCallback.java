
package org.smslib.smsserver.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.callback.IGatewayStatusCallback;
import org.smslib.callback.events.GatewayStatusCallbackEvent;

public class GatewayStatusCallback implements IGatewayStatusCallback
{
	static Logger logger = LoggerFactory.getLogger(GatewayStatusCallback.class);

	@Override
	public boolean process(GatewayStatusCallbackEvent event)
	{
		return true;
	}
}
