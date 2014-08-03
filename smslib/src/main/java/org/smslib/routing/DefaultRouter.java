
package org.smslib.routing;

import java.util.Collection;
import org.smslib.gateway.AbstractGateway;
import org.smslib.message.OutboundMessage;

public class DefaultRouter extends AbstractRouter
{
	@Override
	public Collection<AbstractGateway> customRoute(OutboundMessage message, Collection<AbstractGateway> gateways)
	{
		return gateways;
	}
}
