package org.smslib.examples;

import org.smslib.callback.IInboundMessageCallback;
import org.smslib.callback.events.InboundMessageEvent;
import org.smslib.helper.Log;

public class InboundMessageCallback implements IInboundMessageCallback
{
	@Override
	public boolean process(InboundMessageEvent event)
	{
		Log.getInstance().getLog().info("[InboundMessageCallback] " + event.getMessage().toShortString());
		Log.getInstance().getLog().debug(event.getMessage().toString());
		return true;
	}
}
