
package org.smslib;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.gateway.http.clickatell.Clickatell;
import org.smslib.message.OutboundMessage;

public class Test_Clickatell extends TestCase
{
	static Logger logger = LoggerFactory.getLogger(Test_Clickatell.class);

	String API_ID = "api-id";

	String USERNAME = "username";

	String PASSWORD = "password";

	public void test() throws Exception
	{
		if (this.USERNAME.equalsIgnoreCase("username")) return;
		Service.getInstance().start();
		Clickatell gateway = new Clickatell("clickatell", this.API_ID, this.USERNAME, this.PASSWORD);
		Service.getInstance().registerGateway(gateway);
		OutboundMessage m = new OutboundMessage("306974...", "Hello from 'SMSLib' via Clickatell!");
		Service.getInstance().send(m);
		logger.info(m.toString());
		logger.info("Credit Balance: " + Service.getInstance().queryCreditBalance(gateway));
		logger.info("Delivery: " + Service.getInstance().queryDeliveryStatus(gateway, "d3469fd60909407cc7796463c926adf8"));
		Service.getInstance().unregisterGateway(gateway);
		Service.getInstance().stop();
		Service.getInstance().terminate();
	}
}
