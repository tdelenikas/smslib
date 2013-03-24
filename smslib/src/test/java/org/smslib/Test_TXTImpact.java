
package org.smslib;

import junit.framework.TestCase;
import org.smslib.Service;
import org.smslib.gateway.http.txtimpact.TXTImpact;
import org.smslib.helper.Log;
import org.smslib.message.OutboundMessage;

public class Test_TXTImpact extends TestCase
{
	public void test() throws Exception
	{
		Service.getInstance().start();
		TXTImpact gateway = new TXTImpact("txtimpact", "username", "password", "0", "0");
		Service.getInstance().registerGateway(gateway);
		OutboundMessage m = new OutboundMessage("306974...", "Hello from 'SMSLib' via TXTImpact!");
		Service.getInstance().send(m);
		Log.getInstance().getLog().info(m.toString());
		Log.getInstance().getLog().info("Credit Balance: " + Service.getInstance().queryCreditBalance(gateway));
		Service.getInstance().unregisterGateway(gateway);
		Service.getInstance().stop();
		Service.getInstance().terminate();
	}
}
