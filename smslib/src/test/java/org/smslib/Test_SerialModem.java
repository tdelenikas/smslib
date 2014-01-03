
package org.smslib;

import javax.crypto.spec.SecretKeySpec;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.callback.IDeliveryReportCallback;
import org.smslib.callback.IInboundMessageCallback;
import org.smslib.callback.events.DeliveryReportCallbackEvent;
import org.smslib.callback.events.InboundMessageEvent;
import org.smslib.crypto.AESKey;
import org.smslib.gateway.modem.Modem;
import org.smslib.message.OutboundEncryptedMessage;
import org.smslib.message.OutboundMessage;

public class Test_SerialModem extends TestCase
{
	static Logger logger = LoggerFactory.getLogger(Test_SerialModem.class);

	public static String RECIPIENT = "";

	public class InboundMessageCallback implements IInboundMessageCallback
	{
		@Override
		public boolean process(InboundMessageEvent event)
		{
			logger.info("[InboundMessageCallback] " + event.getMessage().toShortString());
			logger.info(event.getMessage().toString());
			return true;
		}
	}

	public class DeliveryReportCallback implements IDeliveryReportCallback
	{
		@Override
		public boolean process(DeliveryReportCallbackEvent event)
		{
			logger.info("[DeliveryReportCallback] " + event.getMessage().toShortString());
			logger.info(event.getMessage().toString());
			return true;
		}
	}

	public void test() throws Exception
	{
		if (RECIPIENT.length() > 0) Service.getInstance().getKeyManager().registerKey(RECIPIENT, new AESKey(new SecretKeySpec("0011223344556677".getBytes(), "AES")));
		if (true) logger.info("Serial Modem test disabled!");
		else
		{
			Service.getInstance().setInboundMessageCallback(new InboundMessageCallback());
			Service.getInstance().setDeliveryReportCallback(new DeliveryReportCallback());
			Service.getInstance().start();
			Modem gateway = new Modem("modem", "COM4", "19200", "0000", "0000", "3097100000", "");
			Service.getInstance().registerGateway(gateway);
			Thread.sleep(20000);
			if (RECIPIENT.length() > 0)
			{
				logger.info("Sending a simple test message...");
				Service.getInstance().send(new OutboundMessage(RECIPIENT, "Test"));
				Thread.sleep(20000);
				logger.info("Sending an encrypted message...");
				Service.getInstance().send(new OutboundEncryptedMessage(RECIPIENT, "TestABC123".getBytes()));
				Thread.sleep(20000);
			}
			Service.getInstance().unregisterGateway(gateway);
			try
			{
				gateway.refreshDeviceInfo();
				throw new RuntimeException("Should never get here!");
			}
			catch (Exception e)
			{
				//
			}
			Service.getInstance().stop();
			Service.getInstance().terminate();
		}
	}
}
