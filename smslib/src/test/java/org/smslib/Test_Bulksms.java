
package org.smslib;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.gateway.http.bulksms.BulkSmsInternational;
import org.smslib.message.OutboundMessage;

public class Test_Bulksms extends TestCase
{
	static Logger logger = LoggerFactory.getLogger(Test_Bulksms.class);

	String USERNAME = "username";

	String PASSWORD = "password";

	public void test() throws Exception
	{
		if (this.USERNAME.equalsIgnoreCase("username")) return;
		Service.getInstance().start();
		BulkSmsInternational gateway = new BulkSmsInternational("bulksms", this.USERNAME, this.PASSWORD);
		Service.getInstance().registerGateway(gateway);
		OutboundMessage m = new OutboundMessage("306974...", "Hello from 'SMSLib' via BulkSms!");
		Service.getInstance().send(m);
		logger.info(m.toString());
		logger.info("Credit Balance: " + Service.getInstance().queryCreditBalance(gateway));
		logger.info("Delivery: " + Service.getInstance().queryDeliveryStatus(m));
		Service.getInstance().unregisterGateway(gateway);
		Service.getInstance().stop();
		Service.getInstance().terminate();
	}
}
