
package org.smslib;

import junit.framework.TestCase;
import org.smslib.gateway.http.textmagic.TextMagic;
import org.smslib.helper.Log;
import org.smslib.message.MsIsdn;
import org.smslib.message.OutboundMessage;

public class Test_TextMagic extends TestCase
{
	public void test() throws Exception
	{
		Service.getInstance().start();
		TextMagic gateway = new TextMagic("textmagic", "username", "password");
		Service.getInstance().registerGateway(gateway);
		OutboundMessage m = new OutboundMessage("30697", "Hello from 'SMSLib' via TextMagic!");
		m.setOriginator(new MsIsdn("SMSLIB"));
		Service.getInstance().send(m);
		Log.getInstance().getLog().info(m.toString());
		Log.getInstance().getLog().info("Credit Balance: " + Service.getInstance().queryCreditBalance(gateway));
		Log.getInstance().getLog().info("Delivery: " + Service.getInstance().queryDeliveryStatus(m));
		Service.getInstance().unregisterGateway(gateway);
		Service.getInstance().stop();
		Service.getInstance().terminate();
	}
}
