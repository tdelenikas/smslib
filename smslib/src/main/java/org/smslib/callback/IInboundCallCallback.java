
package org.smslib.callback;

import org.smslib.callback.events.InboundCallCallbackEvent;

public interface IInboundCallCallback
{
	public boolean process(InboundCallCallbackEvent event);
}
