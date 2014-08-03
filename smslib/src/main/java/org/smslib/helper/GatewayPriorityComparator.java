
package org.smslib.helper;

import java.io.Serializable;
import java.util.Comparator;
import org.smslib.gateway.AbstractGateway;

public class GatewayPriorityComparator implements Comparator<AbstractGateway>, Serializable
{
	private static final long serialVersionUID = 1L;

	@Override
	public int compare(AbstractGateway g1, AbstractGateway g2)
	{
		return ((g1.getPriority() > g2.getPriority()) ? 1 : ((g1.getPriority() == g2.getPriority() ? 0 : -1)));
	}
}
