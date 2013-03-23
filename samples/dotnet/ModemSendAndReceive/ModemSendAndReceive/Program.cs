using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;

namespace ModemSendAndReceive
{
	class InboundMessageCallback : org.smslib.callback.IInboundMessageCallback
	{
		public Boolean process(org.smslib.callback.events.InboundMessageEvent ev)
		{
			Console.WriteLine("============================================================");
			Console.WriteLine("== I N B O U N D ===========================================");
			Console.WriteLine("============================================================");
			Console.WriteLine("SMS Received: " + ev.getMessage().toShortString());
			Console.WriteLine(ev.getMessage().toString());
			Console.WriteLine("============================================================");
			return true;
		}
	}

	class MessageSentCallback : org.smslib.callback.IMessageSentCallback
	{
		public bool process(org.smslib.callback.events.MessageSentCallbackEvent ev)
		{
			Console.WriteLine("============================================================");
			Console.WriteLine("== M E S S A G E  S E N T ==================================");
			Console.WriteLine("============================================================");
			Console.WriteLine("SMS Sent: " + ev.getMessage().toShortString());
			Console.WriteLine(ev.getMessage().toString());
			Console.WriteLine("============================================================");
			return true;
		}
	}

	class Program
	{
		void RunMe()
		{
			// Start the Comm2IP interface as a in-process thread.
			// TODO: Check and adjust your modem port in the line below!
			Comm2IP.Comm2IP comm2Ip = new Comm2IP.Comm2IP(new byte[] { 127, 0, 0, 1 }, 12000, "COM7", 115200);
			Thread tComm2Ip = new Thread(new ThreadStart(comm2Ip.Run));
			tComm2Ip.Start();

			// Setup some callbacks.
			org.smslib.Service.getInstance().setInboundMessageCallback(new InboundMessageCallback());
			org.smslib.Service.getInstance().setMessageSentCallback(new MessageSentCallback());

			// Define our modem.
			// TODO: Check the PINs and the SMSC number in the line below!
			org.smslib.gateway.modem.Modem modemGateway = new org.smslib.gateway.modem.Modem("modem", "127.0.0.1", 12000, "0000", "0000", new org.smslib.message.MsIsdn("306942190000"));
			// Register our modem.
			org.smslib.Service.getInstance().registerGateway(modemGateway);
			// Start the service.
			org.smslib.Service.getInstance().start();

			// Sleep for a while.
			// During this time, SMSLib will have a chance to read your modem's messages and push
			// them back, via the InboundMessageCallback callback.
			Thread.Sleep(30000);

			// Ok, now let's send a message.
			// TODO: Check the recipient's number in the line below!
			//org.smslib.message.OutboundMessage message = new org.smslib.message.OutboundMessage("306974...", "Hello World!");
			//org.smslib.Service.getInstance().queue(message);

			// Sleep for a while.
			// SMSLib will send the message above, and will call the MessageSentCallback callback in order to inform
			// you of its fate.
			Thread.Sleep(60000);

			// Remove the modem gateway (not needed, but anyway...)
			org.smslib.Service.getInstance().unregisterGateway(modemGateway);

			// Stop all now.
			org.smslib.Service.getInstance().stop();
			org.smslib.Service.getInstance().terminate();

			// Don't forget to stop the Comm2IP thread as well!
			comm2Ip.Stop();
		}

		static void Main(string[] args)
		{
			Program p = new Program();
			try
			{
				p.RunMe();
			}
			catch (Exception e)
			{
				Console.WriteLine(e.Message);
				Console.WriteLine(e.StackTrace);
			}
			Console.WriteLine();
			Console.WriteLine("Press <ENTER> to continue...");
			Console.ReadLine();
		}
	}
}
