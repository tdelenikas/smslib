
package org.smslib;

import junit.framework.TestCase;
import org.smslib.core.Capabilities;
import org.smslib.gateway.MockGateway;
import org.smslib.groups.Group;
import org.smslib.message.MsIsdn;
import org.smslib.message.OutboundMessage;

public class Test_Groups extends TestCase
{
	public void test() throws Exception
	{
		Group g = new Group("my-group-id", "my-group-description");
		assert (g.addAddress(new MsIsdn("0")));
		assert (g.addAddress(new MsIsdn("1")));
		assert (g.addAddress(new MsIsdn("2")));
		assert (g.addAddress(new MsIsdn("3")));
		assert (g.addAddress(new MsIsdn("4")));
		assert (g.addAddress(new MsIsdn("5")));
		assert (g.removeAddress(new MsIsdn("2")));
		assert (!g.removeAddress(new MsIsdn("unknown-recipient")));
		Service.getInstance().addGroup(g);
		assert (!Service.getInstance().getGroupManager().exist("non-existent"));
		assert (Service.getInstance().getGroupManager().exist(g.getName()));
		Service.getInstance().start();
		Capabilities c = new Capabilities();
		c.set(Capabilities.Caps.CanSendMessage);
		MockGateway g1 = new MockGateway("G", "Mock Gateway #1", c, 0, 0);
		Service.getInstance().registerGateway(g1);
		OutboundMessage message = new OutboundMessage("my-group-id", "Hello World!");
		assert (Service.getInstance().queue(message) == 5);
		assert (Service.getInstance().queue(new OutboundMessage("aaaa", "bbbb")) == 1);
		Service.getInstance().unregisterGateway(g1);
		Service.getInstance().stop();
		Service.getInstance().terminate();
	}
}
