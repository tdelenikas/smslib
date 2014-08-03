
package org.smslib.hook;

import org.smslib.message.OutboundMessage;

public interface IPreQueueHook
{
	public boolean process(OutboundMessage message);
}
