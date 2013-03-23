
package org.smslib.callback;

import org.smslib.callback.events.InboundMessageEvent;

public interface IInboundMessageCallback
{
	public boolean process(InboundMessageEvent event);
}
