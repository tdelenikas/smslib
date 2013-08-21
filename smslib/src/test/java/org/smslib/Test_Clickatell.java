
package org.smslib;

import junit.framework.TestCase;
import org.smslib.Service;
import org.smslib.gateway.http.clickatell.Clickatell;
import org.smslib.helper.Log;
import org.smslib.message.OutboundMessage;

public class Test_Clickatell extends TestCase
{
	String API_ID = "api-id";
	String USERNAME = "username";
	String PASSWORD = "password";

	public void test() throws Exception
	{
		if (USERNAME.equalsIgnoreCase("username")) return;
		Service.getInstance().start();
		Clickatell gateway = new Clickatell("clickatell", API_ID, USERNAME, PASSWORD);
		Service.getInstance().registerGateway(gateway);
		OutboundMessage m = new OutboundMessage("306974...", "Hello from 'SMSLib' via Clickatell!");
		Service.getInstance().send(m);
		Log.getInstance().getLog().info(m.toString());
		Log.getInstance().getLog().info("Credit Balance: " + Service.getInstance().queryCreditBalance(gateway));
		Log.getInstance().getLog().info("Delivery: " + Service.getInstance().queryDeliveryStatus(gateway, "d3469fd60909407cc7796463c926adf8"));
		Service.getInstance().unregisterGateway(gateway);
		Service.getInstance().stop();
		Service.getInstance().terminate();
	}
}
