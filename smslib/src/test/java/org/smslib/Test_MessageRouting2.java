
package org.smslib;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.core.Capabilities;
import org.smslib.gateway.MockGateway;
import org.smslib.message.OutboundMessage;
import org.smslib.message.OutboundMessage.SentStatus;

public class Test_MessageRouting2 extends TestCase
{
	static Logger logger = LoggerFactory.getLogger(Test_MessageRouting2.class);

	public void test() throws Exception
	{
		Service.getInstance().setServiceStatusCallback(new ServiceStatusCallback());
		Service.getInstance().setGatewayStatusCallback(new GatewayStatusCallback());
		Service.getInstance().setMessageSentCallback(new MessageSentCallback());
		{
			logger.info("============================");
			logger.info("============================");
			Thread.sleep(5000);
			Service.getInstance().start();
			logger.info("============================");
			logger.info("============================");
			Thread.sleep(5000);
			Service.getInstance().stop();
			logger.info("============================");
			logger.info("============================");
			Thread.sleep(5000);
		}
		{
			int okCount = 0, failCount = 0;
			Service.getInstance().start();
			Capabilities c = new Capabilities();
			c.set(Capabilities.Caps.CanSendMessage);
			MockGateway g1 = new MockGateway("G1", "Mock Gateway #1", c, 0, 0);
			MockGateway g2 = new MockGateway("G2", "Mock Gateway #2", c, 20, 0);
			MockGateway g3 = new MockGateway("G3", "Mock Gateway #3", c, 30, 0);
			MockGateway g4 = new MockGateway("G4", "Mock Gateway #4", c, 50, 0);
			MockGateway g5 = new MockGateway("G5", "Mock Gateway #5", c, 80, 0);
			Service.getInstance().registerGateway(g1);
			Service.getInstance().registerGateway(g2);
			Service.getInstance().registerGateway(g3);
			Service.getInstance().registerGateway(g4);
			Service.getInstance().registerGateway(g5);
			for (int i = 0; i < Limits.NO_OF_MESSAGES; i++)
			{
				OutboundMessage m = new OutboundMessage("3069741234567", "Hello World!");
				Service.getInstance().send(m);
				if (m.getSentStatus() == SentStatus.Sent) okCount++;
				else failCount++;
			}
			logger.info("G1 Traffic = " + g1.getStatistics().getTotalSent());
			logger.info("G2 Traffic = " + g2.getStatistics().getTotalSent());
			logger.info("G3 Traffic = " + g3.getStatistics().getTotalSent());
			logger.info("G4 Traffic = " + g4.getStatistics().getTotalSent());
			logger.info("G5 Traffic = " + g5.getStatistics().getTotalSent());
			logger.info("(SENT)     = " + okCount);
			logger.info("(FAILED)   = " + failCount);
			assert ((okCount + failCount) == Limits.NO_OF_MESSAGES);
			Service.getInstance().unregisterGateway(g1);
			Service.getInstance().unregisterGateway(g2);
			Service.getInstance().unregisterGateway(g3);
			Service.getInstance().unregisterGateway(g4);
			Service.getInstance().unregisterGateway(g5);
			Service.getInstance().stop();
			Service.getInstance().terminate();
		}
	}
}
