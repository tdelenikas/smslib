
package org.smslib;

import javax.crypto.spec.SecretKeySpec;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.callback.IDeliveryReportCallback;
import org.smslib.callback.IInboundMessageCallback;
import org.smslib.callback.events.DeliveryReportCallbackEvent;
import org.smslib.callback.events.InboundMessageCallbackEvent;
import org.smslib.crypto.AESKey;
import org.smslib.gateway.modem.Modem;
import org.smslib.message.OutboundMessage;

public class Test_SerialModem extends TestCase
{
	static Logger logger = LoggerFactory.getLogger(Test_SerialModem.class);

	public static String RECIPIENT = "";

	public class InboundMessageCallback implements IInboundMessageCallback
	{
		@Override
		public boolean process(InboundMessageCallbackEvent event)
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
		Service.getInstance().setInboundMessageCallback(new InboundMessageCallback());
		Service.getInstance().setDeliveryReportCallback(new DeliveryReportCallback());
		Service.getInstance().start();
		Modem gateway = new Modem("modem", "COM1", "19200", "0000", "0000", "", "");
		Service.getInstance().registerGateway(gateway);
		// Print out some device information.
		logger.info("Manufacturer       : " + gateway.getDeviceInformation().getManufacturer());
		logger.info("Signal (RSSI)      : " + gateway.getDeviceInformation().getRssi() + "dBm");
		logger.info("Mode               : " + gateway.getDeviceInformation().getMode());
		logger.info("Supported Encodings: " + gateway.getDeviceInformation().getSupportedEncodings());
		// Sleep to emulate async operation.
		Thread.sleep(20000);
		if (RECIPIENT.length() > 0)
		{
			logger.info("Sending a simple test message...");
			OutboundMessage message = new OutboundMessage(RECIPIENT, "Test");
			message.setRequestDeliveryReport(true);
			if (!Service.getInstance().send(message)) logger.error("The message could NOT be send!");
			//logger.info("Sending an encrypted message...");
			//Service.getInstance().send(new OutboundEncryptedMessage(RECIPIENT, "TestABC123".getBytes()));
			Thread.sleep(60000);
		}
		Service.getInstance().unregisterGateway(gateway);
		try
		{
			gateway.refreshDeviceInfo();
			throw new RuntimeException("Should never get here!");
		}
		catch (Exception e)
		{
			// Normal to get here...
		}
		Service.getInstance().stop();
		Service.getInstance().terminate();
	}
}
