
package org.smslib;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.gateway.http.textmagic.TextMagic;
import org.smslib.message.MsIsdn;
import org.smslib.message.OutboundMessage;

public class Test_TextMagic extends TestCase
{
	static Logger logger = LoggerFactory.getLogger(Test_TextMagic.class);

	public void test() throws Exception
	{
		Service.getInstance().start();
		TextMagic gateway = new TextMagic("textmagic", "username", "password");
		Service.getInstance().registerGateway(gateway);
		OutboundMessage m = new OutboundMessage("30697...", "Hello from 'SMSLib' via TextMagic!");
		m.setOriginatorAddress(new MsIsdn("SMSLIB"));
		Service.getInstance().send(m);
		logger.info(m.toString());
		logger.info("Credit Balance: " + Service.getInstance().queryCreditBalance(gateway));
		logger.info("Delivery: " + Service.getInstance().queryDeliveryStatus(m));
		Service.getInstance().unregisterGateway(gateway);
		Service.getInstance().stop();
		Service.getInstance().terminate();
	}
}
