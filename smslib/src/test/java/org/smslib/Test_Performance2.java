
package org.smslib;

import junit.framework.TestCase;

public class Test_Performance2 extends TestCase
{
	public void test() throws Exception
	{
		// This is a non-normal, never-ending performance test!
		// You wouldn't want to uncomment the code below, as the junit test will never end.
		/*
		Service.getInstance().start();
		Capabilities c = new Capabilities();
		c.set(Capabilities.Caps.CanSendMessage);
		MockGateway g1 = new MockGateway("G1", "Mock Gateway #1", c, 0, 200);
		MockGateway g2 = new MockGateway("G2", "Mock Gateway #2", c, 10, 200);
		MockGateway g3 = new MockGateway("G3", "Mock Gateway #3", c, 20, 200);
		MockGateway g4 = new MockGateway("G4", "Mock Gateway #4", c, 30, 200);
		MockGateway g5 = new MockGateway("G5", "Mock Gateway #5", c, 20, 200);
		Service.getInstance().registerGateway(g1);
		Service.getInstance().registerGateway(g2);
		Service.getInstance().registerGateway(g3);
		Service.getInstance().registerGateway(g4);
		Service.getInstance().registerGateway(g5);
		while (true)
		{
			for (int i = 0; i < 100; i ++) Service.getInstance().queue(new OutboundMessage("1","2"));
			Thread.sleep(9000);
		}
		*/
	}
}
