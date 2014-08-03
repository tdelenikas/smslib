
package org.smslib.hook;

import java.util.Collection;
import org.smslib.gateway.AbstractGateway;
import org.smslib.message.OutboundMessage;

public interface IRouteHook
{
	public Collection<AbstractGateway> process(OutboundMessage message, Collection<AbstractGateway> gateways);
}
