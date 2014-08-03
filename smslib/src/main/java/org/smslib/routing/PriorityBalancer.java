
package org.smslib.routing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.smslib.gateway.AbstractGateway;
import org.smslib.helper.GatewayPriorityComparator;
import org.smslib.message.OutboundMessage;

public class PriorityBalancer extends AbstractBalancer
{
	@Override
	public Collection<AbstractGateway> balance(OutboundMessage message, Collection<AbstractGateway> candidates)
	{
		ArrayList<AbstractGateway> gatewayList = new ArrayList<>(candidates);
		GatewayPriorityComparator comp = new GatewayPriorityComparator();
		Collections.sort(gatewayList, comp);
		Collections.reverse(gatewayList);
		return gatewayList;
	}
}
