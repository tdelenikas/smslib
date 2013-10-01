
package org.smslib;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.gateway.http.txtimpact.TXTImpact;
import org.smslib.message.OutboundMessage;

public class Test_TXTImpact extends TestCase
{
	static Logger logger = LoggerFactory.getLogger(Test_TXTImpact.class);

	public void test() throws Exception
	{
		Service.getInstance().start();
		TXTImpact gateway = new TXTImpact("txtimpact", "username", "password", "0", "0");
		Service.getInstance().registerGateway(gateway);
		OutboundMessage m = new OutboundMessage("306974...", "Hello from 'SMSLib' via TXTImpact!");
		Service.getInstance().send(m);
		logger.info(m.toString());
		logger.info("Credit Balance: " + Service.getInstance().queryCreditBalance(gateway));
		Service.getInstance().unregisterGateway(gateway);
		Service.getInstance().stop();
		Service.getInstance().terminate();
	}
}
