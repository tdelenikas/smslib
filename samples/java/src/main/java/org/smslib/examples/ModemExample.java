
package org.smslib.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.Service;
import org.smslib.gateway.modem.Modem;

public class ModemExample
{
	public static void main(String[] args)
	{
		final Logger logger = LoggerFactory.getLogger(ModemExample.class);
		// Add a couple of callbacks.
		Service.getInstance().setInboundMessageCallback(new InboundMessageCallback());
		Service.getInstance().setMessageSentCallback(new MessageSentCallback());
		Service.getInstance().setServiceStatusCallback(new ServiceStatusCallback());
		Service.getInstance().setGatewayStatusCallback(new GatewayStatusCallback());
		try
		{
			// Start the service.
			Service.getInstance().start();
			// Define and register a modem.
			// TODO: Check the PINs and the SMSC number in the line below!
			Modem modemGateway = new Modem("modem", "COM8", 19200, "0000", "", new org.smslib.message.MsIsdn("306942190000"), "");
			Service.getInstance().registerGateway(modemGateway);
			// Sleep for a minute, simulating the asynchronous concept.
			// Inbound messages will activate your InboundMessageCallback method.
			Thread.sleep(60000);
			// Send a message and go back to sleep for a minute.
			// When dispatched, your MessageSentCallback will be called.
			// TODO: Check the recipient's number in the line below!
			//OutboundMessage message = new OutboundMessage("306974...", "Hello World!");
			//Service.getInstance().queue(message);
			Thread.sleep(60000);
			// Get rid of the gateway!
			Service.getInstance().unregisterGateway(modemGateway);
		}
		catch (InterruptedException e)
		{
			logger.info("OK - now exiting...");
		}
		finally
		{
			// Shutdown the service.
			try
			{
				Service.getInstance().stop();
				Service.getInstance().terminate();
			}
			catch (Exception e)
			{
				logger.error("Oops... Something went wrong!!!", e);
			}
		}
	}
}
