
package org.smslib;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.gateway.http.nexmo.Nexmo;
import org.smslib.message.MsIsdn;
import org.smslib.message.OutboundMessage;

public class Test_Nexmo extends TestCase
{
	static Logger logger = LoggerFactory.getLogger(Test_Nexmo.class);

	String API_ID = "api-id";

	String SECRET = "secret";

	public void test() throws Exception
	{
		if (this.API_ID.equalsIgnoreCase("api-id")) return;
		Service.getInstance().start();
		Nexmo gateway = new Nexmo("nexmo", this.API_ID, this.SECRET);
		Service.getInstance().registerGateway(gateway);
		OutboundMessage m = new OutboundMessage("306974...", "Hello from 'SMSLib' via Nexmo!");
		m.setOriginatorAddress(new MsIsdn("SMSLIB"));
		Service.getInstance().send(m);
		logger.info(m.toString());
		logger.info("Credit Balance: " + Service.getInstance().queryCreditBalance(gateway));
		Service.getInstance().unregisterGateway(gateway);
		Service.getInstance().stop();
		Service.getInstance().terminate();
	}
}
