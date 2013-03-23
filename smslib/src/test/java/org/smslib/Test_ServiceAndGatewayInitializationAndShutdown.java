
package org.smslib;

import junit.framework.TestCase;
import org.smslib.core.Capabilities;
import org.smslib.gateway.AbstractGateway;
import org.smslib.gateway.MockGateway;

public class Test_ServiceAndGatewayInitializationAndShutdown extends TestCase
{
	public void test() throws Exception
	{
		Service.getInstance().setServiceStatusCallback(new ServiceStatusCallback());
		Service.getInstance().setGatewayStatusCallback(new GatewayStatusCallback());
		//
		Service.getInstance().start();
		Thread.sleep(2000);
		Service.getInstance().stop();
		Thread.sleep(2000);
		Service.getInstance().start();
		Thread.sleep(2000);
		Service.getInstance().stop();
		//
		MockGateway g1 = new MockGateway("G1", "Mock Gateway #1", new Capabilities(), 0, 0);
		//
		Service.getInstance().registerGateway(g1);
		assert (g1.getStatus() == AbstractGateway.Status.Stopped);
		Service.getInstance().unregisterGateway(g1);
		//
		Service.getInstance().start();
		Service.getInstance().registerGateway(g1);
		assert (g1.getStatus() == AbstractGateway.Status.Started);
		Thread.sleep(2000);
		Service.getInstance().unregisterGateway(g1);
		assert (g1.getStatus() == AbstractGateway.Status.Stopped);
		Service.getInstance().stop();
		//
		Service.getInstance().registerGateway(g1);
		Service.getInstance().start();
		assert (g1.getStatus() == AbstractGateway.Status.Started);
		Thread.sleep(2000);
		Service.getInstance().stop();
		assert (g1.getStatus() == AbstractGateway.Status.Stopped);
		//
		Thread.sleep(1000);
		//
		Service.getInstance().setServiceStatusCallback(null);
		Service.getInstance().setGatewayStatusCallback(null);
	}
}
