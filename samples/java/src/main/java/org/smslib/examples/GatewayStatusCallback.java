package org.smslib.examples;

import org.smslib.callback.IGatewayStatusCallback;
import org.smslib.callback.events.GatewayStatusCallbackEvent;
import org.smslib.helper.Log;

public class GatewayStatusCallback implements IGatewayStatusCallback
{
	@Override
	public boolean process(GatewayStatusCallbackEvent event)
	{
		Log.getInstance().getLog().info("[GatewayStatusCallback] " + event.getOldStatus() + " -> " + event.getNewStatus());
		return true;
	}
}
