
package org.smslib.helper;

import java.io.Serializable;
import java.util.Comparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.gateway.AbstractGateway;

public class GatewayOutboundTrafficComparator implements Comparator<AbstractGateway>, Serializable
{
	static Logger logger = LoggerFactory.getLogger(GatewayOutboundTrafficComparator.class);

	private static final long serialVersionUID = 1L;

	@Override
	public int compare(AbstractGateway g1, AbstractGateway g2)
	{
		try
		{
			return (((g1.getStatistics().getTotalSent() + g1.getQueueLoad()) > (g2.getStatistics().getTotalSent() + g2.getQueueLoad())) ? 1 : (((g1.getStatistics().getTotalSent() + g1.getQueueLoad()) == (g2.getStatistics().getTotalSent() + g2.getQueueLoad()) ? 0 : -1)));
		}
		catch (Exception e)
		{
			logger.error("Unhandled exception!", e);
			return 0;
		}
	}
}
