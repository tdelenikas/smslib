
package org.smslib.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.callback.IInboundMessageCallback;
import org.smslib.callback.events.InboundMessageEvent;

public class InboundMessageCallback implements IInboundMessageCallback
{
	final Logger logger = LoggerFactory.getLogger(InboundMessageCallback.class);

	@Override
	public boolean process(InboundMessageEvent event)
	{
		logger.info("[InboundMessageCallback] " + event.getMessage().toShortString());
		logger.debug(event.getMessage().toString());
		return true;
	}
}
