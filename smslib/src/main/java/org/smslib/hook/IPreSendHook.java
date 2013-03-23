
package org.smslib.hook;

import org.smslib.message.OutboundMessage;

public interface IPreSendHook
{
	public boolean process(OutboundMessage message);
}
