
package org.smslib.callback;

import org.smslib.callback.events.InboundCallEvent;

public interface IInboundCallCallback
{
	public boolean process(InboundCallEvent event);
}
