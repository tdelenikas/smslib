
package org.smslib;

import junit.framework.TestCase;
import org.smslib.Service;
import org.smslib.gateway.http.nexmo.Nexmo;
import org.smslib.helper.Log;
import org.smslib.message.MsIsdn;
import org.smslib.message.OutboundMessage;

public class Test_Nexmo extends TestCase
{
	String API_ID = "api-id";
	String SECRET = "secret";

	public void test() throws Exception
	{
		if (API_ID.equalsIgnoreCase("api-id")) return;
		Service.getInstance().start();
		Nexmo gateway = new Nexmo("nexmo", API_ID, SECRET);
		Service.getInstance().registerGateway(gateway);
		OutboundMessage m = new OutboundMessage("306974...", "Hello from 'SMSLib' via Nexmo!");
		m.setOriginator(new MsIsdn("SMSLIB"));
		Service.getInstance().send(m);
		Log.getInstance().getLog().info(m.toString());
		Log.getInstance().getLog().info("Credit Balance: " + Service.getInstance().queryCreditBalance(gateway));
		Service.getInstance().unregisterGateway(gateway);
		Service.getInstance().stop();
		Service.getInstance().terminate();
	}
}
