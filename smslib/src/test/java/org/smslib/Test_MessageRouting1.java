
package org.smslib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.core.Capabilities;
import org.smslib.core.Capabilities.Caps;
import org.smslib.gateway.AbstractGateway;
import org.smslib.gateway.MockGateway;
import org.smslib.hook.IRouteHook;
import org.smslib.message.MsIsdn;
import org.smslib.message.OutboundMessage;
import org.smslib.message.OutboundMessage.FailureCause;
import org.smslib.message.OutboundMessage.SentStatus;
import org.smslib.routing.DefaultRouter;
import org.smslib.routing.NumberRouter;

public class Test_MessageRouting1 extends TestCase
{
	static Logger logger = LoggerFactory.getLogger(Test_MessageRouting1.class);

	class RouteMessageHook implements IRouteHook
	{
		@Override
		public Collection<AbstractGateway> process(OutboundMessage message, Collection<AbstractGateway> gateways)
		{
			Collection<AbstractGateway> newRoutes = new ArrayList<>();
			Iterator<AbstractGateway> i = gateways.iterator();
			while (i.hasNext())
			{
				AbstractGateway g = i.next();
				if (g.getGatewayId().equalsIgnoreCase("G2")) newRoutes.add(g);
			}
			return newRoutes;
		}
	}

	public void test() throws Exception
	{
		Service.getInstance().setServiceStatusCallback(new ServiceStatusCallback());
		Service.getInstance().setGatewayStatusCallback(new GatewayStatusCallback());
		{
			logger.info("Step #1");
			Capabilities c = new Capabilities();
			c.set(Capabilities.Caps.CanSendMessage);
			MockGateway g = new MockGateway("G1", "Mock Gateway #1", c, 0, 0);
			Service.getInstance().registerGateway(g);
			OutboundMessage m = new OutboundMessage("3069741234567", "Hello World!");
			Service.getInstance().send(m);
			assert (m.getSentStatus() == SentStatus.Failed);
			assert (m.getFailureCause() == FailureCause.NoService);
			assert (g.getStatistics().getTotalSent() == 0);
			assert (g.getStatistics().getTotalFailures() == 0);
			Service.getInstance().unregisterGateway(g);
		}
		{
			logger.info("Step #1.1");
			Capabilities c = new Capabilities();
			c.set(Capabilities.Caps.CanSendMessage);
			MockGateway g = new MockGateway("G1", "Mock Gateway #1", c, 100, 0);
			Service.getInstance().registerGateway(g);
			OutboundMessage m = new OutboundMessage("3069741234567", "Hello World!");
			Service.getInstance().send(m);
			assert (m.getSentStatus() == SentStatus.Failed);
			assert (m.getFailureCause() == FailureCause.NoService);
			assert (g.getStatistics().getTotalSent() == 0);
			assert (g.getStatistics().getTotalFailures() == 0);
			Service.getInstance().unregisterGateway(g);
		}
		//
		Service.getInstance().start();
		//
		{
			logger.info("Step #2");
			Capabilities c = new Capabilities();
			MockGateway g = new MockGateway("G1", "Mock Gateway #1", c, 0, 0);
			Service.getInstance().registerGateway(g);
			OutboundMessage m = new OutboundMessage("3069741234567", "Hello World!");
			Service.getInstance().send(m);
			assert (m.getSentStatus() == SentStatus.Failed);
			assert (m.getFailureCause() == FailureCause.NoRoute);
			Service.getInstance().unregisterGateway(g);
		}
		{
			logger.info("Step #3");
			Capabilities c = new Capabilities();
			c.set(Capabilities.Caps.CanSendMessage);
			c.set(Capabilities.Caps.CanSplitMessages);
			MockGateway g = new MockGateway("G1", "Mock Gateway #1", c, 0, 0);
			Service.getInstance().registerGateway(g);
			OutboundMessage m = new OutboundMessage("3069741234567", "Hello World!");
			m.setOriginatorAddress(new MsIsdn("AAA"));
			Service.getInstance().send(m);
			assert (m.getSentStatus() == SentStatus.Failed);
			assert (m.getFailureCause() == FailureCause.NoRoute);
			assert (g.getStatistics().getTotalSent() == 0);
			assert (g.getStatistics().getTotalFailures() == 0);
			Service.getInstance().unregisterGateway(g);
		}
		{
			logger.info("Step #4");
			Capabilities c = new Capabilities();
			c.set(Capabilities.Caps.CanSendMessage);
			MockGateway g1 = new MockGateway("G1", "Mock Gateway #1", c, 0, 0);
			MockGateway g2 = new MockGateway("G2", "Mock Gateway #2", c, 0, 0);
			Service.getInstance().registerGateway(g1);
			Service.getInstance().registerGateway(g2);
			for (int i = 0; i < Limits.NO_OF_MESSAGES; i++)
			{
				OutboundMessage m = new OutboundMessage("3069741234567", "Hello World!");
				Service.getInstance().send(m);
				assert (m.getSentStatus() == SentStatus.Sent);
				assert (m.getGatewayId().equalsIgnoreCase("G1") || m.getGatewayId().equalsIgnoreCase("G2"));
			}
			logger.info("G1 Traffic = " + g1.getStatistics().getTotalSent());
			logger.info("G2 Traffic = " + g2.getStatistics().getTotalSent());
			assert (g1.getStatistics().getTotalSent() == (Limits.NO_OF_MESSAGES / 2));
			assert (g2.getStatistics().getTotalSent() == (Limits.NO_OF_MESSAGES / 2));
			Service.getInstance().unregisterGateway(g1);
			Service.getInstance().unregisterGateway(g2);
		}
		{
			logger.info("Step #5");
			Capabilities c = new Capabilities();
			c.set(Capabilities.Caps.CanSendMessage);
			MockGateway g1 = new MockGateway("G1", "Mock Gateway #1", c, 0, 0);
			MockGateway g2 = new MockGateway("G2", "Mock Gateway #2", c, 0, 0);
			MockGateway g3 = new MockGateway("G3", "Mock Gateway #3", new Capabilities(), 0, 0);
			Service.getInstance().registerGateway(g1);
			Service.getInstance().registerGateway(g2);
			Service.getInstance().registerGateway(g3);
			for (int i = 0; i < Limits.NO_OF_MESSAGES; i++)
			{
				OutboundMessage m = new OutboundMessage("3069741234567", "Hello World!");
				Service.getInstance().send(m);
				assert (m.getSentStatus() == SentStatus.Sent);
				assert (m.getGatewayId().equalsIgnoreCase("G1") || m.getGatewayId().equalsIgnoreCase("G2"));
			}
			logger.info("G1 Traffic = " + g1.getStatistics().getTotalSent());
			logger.info("G2 Traffic = " + g2.getStatistics().getTotalSent());
			logger.info("G3 Traffic = " + g3.getStatistics().getTotalSent());
			assert (g1.getStatistics().getTotalSent() == (Limits.NO_OF_MESSAGES / 2));
			assert (g2.getStatistics().getTotalSent() == (Limits.NO_OF_MESSAGES / 2));
			assert (g3.getStatistics().getTotalSent() == 0);
			Service.getInstance().unregisterGateway(g1);
			Service.getInstance().unregisterGateway(g2);
			Service.getInstance().unregisterGateway(g3);
		}
		{
			logger.info("Step #6");
			Capabilities c = new Capabilities();
			c.set(Capabilities.Caps.CanSendMessage);
			MockGateway g1 = new MockGateway("G1", "Mock Gateway #1", c, 0, 0);
			MockGateway g2 = new MockGateway("G2", "Mock Gateway #2", c, 0, 0);
			MockGateway g3 = new MockGateway("G3", "Mock Gateway #3", c, 0, 0);
			Service.getInstance().registerGateway(g1);
			Service.getInstance().registerGateway(g2);
			Service.getInstance().registerGateway(g3);
			NumberRouter router = new NumberRouter();
			router.addRule("30\\d+", g1);
			router.addRule("31\\d+", g2);
			Service.getInstance().setRouter(router);
			for (int i = 0; i < Limits.NO_OF_MESSAGES; i++)
			{
				OutboundMessage m = new OutboundMessage("3069741234567", "Hello World!");
				Service.getInstance().send(m);
				assert (m.getSentStatus() == SentStatus.Sent);
				assert (m.getGatewayId().equalsIgnoreCase("G1"));
			}
			assert (g1.getStatistics().getTotalSent() == Limits.NO_OF_MESSAGES);
			for (int i = 0; i < Limits.NO_OF_MESSAGES; i++)
			{
				OutboundMessage m = new OutboundMessage("3169741234567", "Hello World!");
				Service.getInstance().send(m);
				assert (m.getSentStatus() == SentStatus.Sent);
				assert (m.getGatewayId().equalsIgnoreCase("G2"));
			}
			assert (g2.getStatistics().getTotalSent() == Limits.NO_OF_MESSAGES);
			for (int i = 0; i < Limits.NO_OF_MESSAGES; i++)
			{
				OutboundMessage m = new OutboundMessage("3269741234567", "Hello World!");
				Service.getInstance().send(m);
				assert (m.getSentStatus() == SentStatus.Failed);
				assert (m.getFailureCause() == FailureCause.NoRoute);
			}
			assert (g1.getStatistics().getTotalSent() == Limits.NO_OF_MESSAGES);
			assert (g2.getStatistics().getTotalSent() == Limits.NO_OF_MESSAGES);
			Service.getInstance().setRouter(new DefaultRouter());
			Service.getInstance().unregisterGateway(g1);
			Service.getInstance().unregisterGateway(g2);
			Service.getInstance().unregisterGateway(g3);
		}
		{
			logger.info("Step #7");
			Capabilities c = new Capabilities();
			c.set(Capabilities.Caps.CanSendMessage);
			MockGateway g1 = new MockGateway("G1", "Mock Gateway #1", c, 0, 0);
			MockGateway fg1 = new MockGateway("G2", "Mock Gateway #2", c, 100, 0);
			Service.getInstance().registerGateway(g1);
			Service.getInstance().registerGateway(fg1);
			for (int i = 0; i < Limits.NO_OF_MESSAGES; i++)
			{
				OutboundMessage m = new OutboundMessage("3269741234567", "Hello World!");
				Service.getInstance().send(m);
				assert (m.getSentStatus() == SentStatus.Sent);
				assert (m.getGatewayId().equalsIgnoreCase("G1"));
			}
			assert (g1.getStatistics().getTotalSent() == Limits.NO_OF_MESSAGES);
			assert (fg1.getStatistics().getTotalSent() == 0);
			Service.getInstance().unregisterGateway(g1);
			Service.getInstance().unregisterGateway(fg1);
		}
		{
			logger.info("Step #8");
			Service.getInstance().setRouteHook(new RouteMessageHook());
			Capabilities c = new Capabilities();
			c.set(Caps.CanSendMessage);
			MockGateway g1 = new MockGateway("G1", "Mock Gateway #1", c, 0, 0);
			Service.getInstance().registerGateway(g1);
			MockGateway g2 = new MockGateway("G2", "Mock Gateway #2", c, 0, 0);
			Service.getInstance().registerGateway(g2);
			MockGateway g3 = new MockGateway("G3", "Mock Gateway #3", c, 0, 0);
			Service.getInstance().registerGateway(g3);
			for (int i = 0; i < Limits.NO_OF_MESSAGES; i++)
			{
				OutboundMessage m = new OutboundMessage("3069741234567", "Hello World!");
				Service.getInstance().send(m);
				assert (m.getSentStatus() == SentStatus.Sent);
				assert (m.getGatewayId().equalsIgnoreCase("G2"));
			}
			logger.info("G1 Traffic = " + g1.getStatistics().getTotalSent());
			logger.info("G2 Traffic = " + g2.getStatistics().getTotalSent());
			logger.info("G3 Traffic = " + g3.getStatistics().getTotalSent());
			assert (g1.getStatistics().getTotalSent() == 0);
			assert (g2.getStatistics().getTotalSent() == Limits.NO_OF_MESSAGES);
			assert (g3.getStatistics().getTotalSent() == 0);
			Service.getInstance().unregisterGateway(g1);
			Service.getInstance().unregisterGateway(g2);
			Service.getInstance().unregisterGateway(g3);
			Service.getInstance().setRouteHook(null);
		}
		Service.getInstance().stop();
		Service.getInstance().terminate();
		//
		Service.getInstance().setServiceStatusCallback(null);
		Service.getInstance().setGatewayStatusCallback(null);
	}
}
