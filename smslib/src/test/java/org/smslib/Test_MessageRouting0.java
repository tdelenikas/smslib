
package org.smslib;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.core.Capabilities;
import org.smslib.gateway.MockGateway;
import org.smslib.message.OutboundMessage;

public class Test_MessageRouting0 extends TestCase
{
	static Logger logger = LoggerFactory.getLogger(Test_MessageRouting0.class);

	public void test() throws Exception
	{
		Service.getInstance().start();
		//
		Capabilities c = new Capabilities();
		c.set(Capabilities.Caps.CanSendMessage);
		MockGateway g1 = new MockGateway("G1", "Mock Gateway #1", c, 0, 200);
		MockGateway g2 = new MockGateway("G2", "Mock Gateway #2", c, 10, 200);
		Service.getInstance().registerGateway(g1);
		Service.getInstance().registerGateway(g2);
		for (int i = 0; i < Limits.NO_OF_MESSAGES; i++)
		{
			OutboundMessage m = new OutboundMessage("3069741234567", "Hello World!");
			Service.getInstance().send(m);
		}
		logger.info("G1 Traffic = " + g1.getStatistics().getTotalSent());
		logger.info("G2 Traffic = " + g2.getStatistics().getTotalSent());
		//
		for (int i = 0; i < Limits.NO_OF_MESSAGES; i++)
		{
			OutboundMessage m = new OutboundMessage("3069741234567", "Hello World!");
			Service.getInstance().queue(m);
		}
		//
		while (Service.getInstance().getAllQueueLoad() > 0)
			Thread.sleep(5000);
		//
		logger.info("G1 Traffic = " + g1.getStatistics().getTotalSent());
		logger.info("G2 Traffic = " + g2.getStatistics().getTotalSent());
		//
		Service.getInstance().stop();
		Service.getInstance().terminate();
	}
}
