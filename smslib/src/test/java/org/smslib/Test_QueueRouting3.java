
package org.smslib;

import java.util.Date;
import java.util.Random;
import junit.framework.TestCase;
import org.smslib.callback.IMessageSentCallback;
import org.smslib.callback.events.MessageSentCallbackEvent;
import org.smslib.core.Capabilities;
import org.smslib.gateway.AbstractGateway.Status;
import org.smslib.gateway.MockGateway;
import org.smslib.helper.Log;
import org.smslib.message.OutboundMessage;
import org.smslib.message.OutboundMessage.SentStatus;

public class Test_QueueRouting3 extends TestCase
{
	int GATEWAY_COUNT = 10;

	int failed = 0;

	int sent = 0;

	class MessageSentCallback implements IMessageSentCallback
	{
		@Override
		public boolean process(MessageSentCallbackEvent event)
		{
			if (event.getMessage().getSentStatus() == SentStatus.Failed) Test_QueueRouting3.this.failed++;
			else if (event.getMessage().getSentStatus() == SentStatus.Sent) Test_QueueRouting3.this.sent++;
			else throw new RuntimeException("Invalid status -> " + event.getMessage().getSentStatus());
			return true;
		}
	}

	public void test() throws Exception
	{
		MockGateway[] g = new MockGateway[this.GATEWAY_COUNT];
		Service.getInstance().setMessageSentCallback(new MessageSentCallback());
		Service.getInstance().start();
		Capabilities c = new Capabilities();
		c.set(Capabilities.Caps.CanSendMessage);
		for (int i = 0; i < this.GATEWAY_COUNT; i++)
		{
			if (i >= 3) g[i] = new MockGateway("G" + i, "Mock Gateway #" + i, c, 0, 100);
			else g[i] = new MockGateway("G" + i, "Mock Gateway #" + i, c, 10, 100);
			Service.getInstance().registerGateway(g[i]);
		}
		Log.getInstance().getLog().info("Queueing " + Limits.NO_OF_MESSAGES + " messages...");
		for (int i = 0; i < Limits.NO_OF_MESSAGES; i++)
			assert (Service.getInstance().queue(new OutboundMessage("306974000000", "Hello World! (queued)")) == 1);
		while (true)
		{
			if (Service.getInstance().getAllQueueLoad() == 0) break;
			Random rand = new Random(new Date().getTime());
			int whichGateway = (rand.nextInt() % 10);
			if (whichGateway < 0) whichGateway *= -1;
			if (g[whichGateway].getStatus() == Status.Started)
			{
				g[whichGateway].stop();
				Log.getInstance().getLog().info("Out of pool : #" + whichGateway);
			}
			else
			{
				g[whichGateway].start();
				Log.getInstance().getLog().info("In pool : #" + whichGateway);
			}
			Thread.sleep(1000);
		}
		while (Service.getInstance().getAllQueueLoad() != 0)
			Thread.sleep(1000);
		Thread.sleep(1000);
		int grandTotal = 0;
		for (int i = 0; i < this.GATEWAY_COUNT; i++)
		{
			Log.getInstance().getLog().info("G" + i + " = " + g[i].getStatistics().getTotalSent());
			grandTotal += g[i].getStatistics().getTotalSent();
		}
		Log.getInstance().getLog().info("SENT TRAFFIC");
		Log.getInstance().getLog().info("G-TOTAL  = " + grandTotal);
		Log.getInstance().getLog().info("(SEND)   = " + this.sent);
		Log.getInstance().getLog().info("(FAILED) = " + this.failed);
		assert (grandTotal + this.failed == Limits.NO_OF_MESSAGES);
		assert (this.sent + this.failed == Limits.NO_OF_MESSAGES);
		for (int i = 0; i < this.GATEWAY_COUNT; i++)
			Service.getInstance().unregisterGateway(g[i]);
		Service.getInstance().stop();
		Service.getInstance().terminate();
	}
}
