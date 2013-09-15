
package org.smslib;

import javax.crypto.spec.SecretKeySpec;
import junit.framework.TestCase;
import org.smslib.callback.IDeliveryReportCallback;
import org.smslib.callback.IInboundMessageCallback;
import org.smslib.callback.events.DeliveryReportCallbackEvent;
import org.smslib.callback.events.InboundMessageEvent;
import org.smslib.crypto.AESKey;
import org.smslib.gateway.modem.Modem;
import org.smslib.helper.Log;
import org.smslib.message.OutboundEncryptedMessage;
import org.smslib.message.OutboundMessage;

public class Test_SerialModem extends TestCase
{
	public static String RECIPIENT = "";

	public class InboundMessageCallback implements IInboundMessageCallback
	{
		@Override
		public boolean process(InboundMessageEvent event)
		{
			Log.getInstance().getLog().info("[InboundMessageCallback] " + event.getMessage().toShortString());
			Log.getInstance().getLog().info(event.getMessage().toString());
			return true;
		}
	}

	public class DeliveryReportCallback implements IDeliveryReportCallback
	{
		@Override
		public boolean process(DeliveryReportCallbackEvent event)
		{
			Log.getInstance().getLog().info("[DeliveryReportCallback] " + event.getMessage().toShortString());
			Log.getInstance().getLog().info(event.getMessage().toString());
			return true;
		}
	}

	public void test() throws Exception
	{
		if (true) Log.getInstance().getLog().info("Serial Modem test disabled!");
		else
		{
			Service.getInstance().setInboundMessageCallback(new InboundMessageCallback());
			Service.getInstance().setDeliveryReportCallback(new DeliveryReportCallback());
			Service.getInstance().start();
			Modem gateway = new Modem("modem1", "COM6", "19200", "0000", "0000", "306942190000", "");
			Service.getInstance().registerGateway(gateway);
			Thread.sleep(20000);
			if (RECIPIENT.length() > 0)
			{
				Log.getInstance().getLog().info("Sending a simple test message...");
				Service.getInstance().send(new OutboundMessage(RECIPIENT, "Test"));
				Thread.sleep(20000);
				//Log.getInstance().getLog().info("Sending an encrypted message...");
				//Service.getInstance().getKeyManager().registerKey(RECIPIENT, new AESKey(new SecretKeySpec("0011223344556677".getBytes(), "AES")));
				//Service.getInstance().send(new OutboundEncryptedMessage(RECIPIENT, "Test".getBytes()));
				//Thread.sleep(20000);
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
