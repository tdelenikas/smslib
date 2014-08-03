
package org.smslib.routing;

import java.util.Collection;
import org.smslib.gateway.AbstractGateway;
import org.smslib.message.OutboundMessage;

public abstract class AbstractBalancer
{
	public abstract Collection<AbstractGateway> balance(OutboundMessage message, Collection<AbstractGateway> candidates);
}
