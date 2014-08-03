
package org.smslib.routing;

import java.util.ArrayList;
import java.util.Collection;
import org.smslib.gateway.AbstractGateway;
import org.smslib.gateway.AbstractGateway.Status;
import org.smslib.helper.Common;
import org.smslib.message.OutboundMessage;

public abstract class AbstractRouter
{
	public Collection<AbstractGateway> route(OutboundMessage message, Collection<AbstractGateway> gateways)
	{
		AbstractGateway candidateGateway;
		ArrayList<AbstractGateway> candidateGateways = new ArrayList<>();
		for (AbstractGateway g : gateways)
		{
			candidateGateway = null;
			if (Common.isNullOrEmpty(message.getGatewayId())) candidateGateway = g;
			else if (message.getGatewayId().equalsIgnoreCase(g.getGatewayId())) candidateGateway = g;
			if (candidateGateway == null) continue;
			if (candidateGateway.getStatus() != Status.Started) continue;
			if (!candidateGateway.getCapabilities().matches(message)) continue;
			candidateGateways.add(candidateGateway);
		}
		return customRoute(message, candidateGateways);
	}

	public abstract Collection<AbstractGateway> customRoute(OutboundMessage msg, Collection<AbstractGateway> gateways);
}
