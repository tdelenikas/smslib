
package org.smslib.callback;

import org.smslib.callback.events.InboundMessageCallbackEvent;

public interface IInboundMessageCallback
{
	public boolean process(InboundMessageCallbackEvent event);
}
