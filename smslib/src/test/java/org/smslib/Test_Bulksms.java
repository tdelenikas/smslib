
package org.smslib;

import junit.framework.TestCase;
import org.smslib.Service;
import org.smslib.gateway.http.bulksms.BulkSmsInternational;
import org.smslib.helper.Log;
import org.smslib.message.OutboundMessage;

public class Test_Bulksms extends TestCase
{
	String USERNAME = "username";
	String PASSWORD = "password";

	public void test() throws Exception
	{
		if (USERNAME.equalsIgnoreCase("username")) return;
		Service.getInstance().start();
		BulkSmsInternational gateway = new BulkSmsInternational("bulksms", USERNAME, PASSWORD);
		Service.getInstance().registerGateway(gateway);
		OutboundMessage m = new OutboundMessage("306974...", "Hello from 'SMSLib' via BulkSms!");
		Service.getInstance().send(m);
		Log.getInstance().getLog().info(m.toString());
		Log.getInstance().getLog().info("Credit Balance: " + Service.getInstance().queryCreditBalance(gateway));
		Log.getInstance().getLog().info("Delivery: " + Service.getInstance().queryDeliveryStatus(m));
		Service.getInstance().unregisterGateway(gateway);
		Service.getInstance().stop();
		Service.getInstance().terminate();
	}
}
