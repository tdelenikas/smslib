
package org.smslib;

import junit.framework.TestCase;
import org.smslib.callback.IDequeueMessageCallback;
import org.smslib.callback.IMessageSentCallback;
import org.smslib.callback.events.DequeueMessageCallbackEvent;
import org.smslib.callback.events.MessageSentCallbackEvent;
import org.smslib.core.Capabilities;
import org.smslib.core.Settings;
import org.smslib.gateway.MockGateway;
import org.smslib.helper.Log;
import org.smslib.message.OutboundMessage;
import org.smslib.message.OutboundMessage.FailureCause;
import org.smslib.message.OutboundMessage.SentStatus;

public class Test_QueueRouting5 extends TestCase
{
	int GATEWAY_COUNT = 10;

	int failed = 0;

	int sent = 0;

	int noRouteFailed = 0;

	int dequeue = 0;

	class MessageSentCallback implements IMessageSentCallback
	{
		@Override
		public boolean process(MessageSentCallbackEvent event)
		{
			if (event.getMessage().getSentStatus() == SentStatus.Failed)
			{
				Test_QueueRouting5.this.failed++;
				if (event.getMessage().getFailureCause() == FailureCause.NoRoute) Test_QueueRouting5.this.noRouteFailed++;
			}
			else if (event.getMessage().getSentStatus() == SentStatus.Sent) Test_QueueRouting5.this.sent++;
			else throw new RuntimeException("Invalid status -> " + event.getMessage().getSentStatus());
			return true;
		}
	}

	class DequeueMessageCallback implements IDequeueMessageCallback
	{
		@Override
		public boolean process(DequeueMessageCallbackEvent event)
		{
			Test_QueueRouting5.this.dequeue++;
			return true;
		}
	}

	public void test() throws Exception
	{
		Settings.keepOutboundMessagesInQueue = true;
		MockGateway[] g = new MockGateway[this.GATEWAY_COUNT];
		Service.getInstance().setMessageSentCallback(new MessageSentCallback());
		Service.getInstance().setDequeueMessageCallback(new DequeueMessageCallback());
		Service.getInstance().start();
		Capabilities c = new Capabilities();
		c.set(Capabilities.Caps.CanSendMessage);
		Log.getInstance().getLog().info("Queueing " + Limits.NO_OF_MESSAGES + " messages...");
		for (int i = 0; i < Limits.NO_OF_MESSAGES; i++)
			assert (Service.getInstance().queue(new OutboundMessage("306974000000", "Hello World! (queued)")) == 1);
		Thread.sleep(1000);
		Log.getInstance().getLog().info("QUEUE LOAD (MASTER) = " + Service.getInstance().getMasterQueueLoad());
		if (Settings.keepOutboundMessagesInQueue) assert (Limits.NO_OF_MESSAGES == Service.getInstance().getMasterQueueLoad());
		for (int i = 0; i < this.GATEWAY_COUNT; i++)
		{
			if (i >= 3) g[i] = new MockGateway("G" + i, "Mock Gateway #" + i, c, 0, 100);
			else g[i] = new MockGateway("G" + i, "Mock Gateway #" + i, c, 10, 100);
			Service.getInstance().registerGateway(g[i]);
		}
		Log.getInstance().getLog().info("Sleeping for a while...");
		Thread.sleep(10000);
		Log.getInstance().getLog().info("Now shutdown all gateways!");
		int grandTotal = 0;
		for (int i = 0; i < this.GATEWAY_COUNT; i++)
		{
			Service.getInstance().unregisterGateway(g[i]);
			grandTotal += g[i].getStatistics().getTotalSent();
		}
		Thread.sleep(1000);
		Log.getInstance().getLog().info("SENT TRAFFIC");
		Log.getInstance().getLog().info("G-TOTAL = " + grandTotal);
		Log.getInstance().getLog().info("SENT = " + this.sent);
		Log.getInstance().getLog().info("FAILED = " + this.failed);
		Log.getInstance().getLog().info("QUEUE LOAD (ALL) = " + Service.getInstance().getAllQueueLoad());
		Log.getInstance().getLog().info("QUEUE LOAD (MASTER) = " + Service.getInstance().getMasterQueueLoad());
		Log.getInstance().getLog().info("GATEWAYS' LIST = " + Service.getInstance().getGatewayIDs().size());
		Log.getInstance().getLog().info("DEQUEUE = " + this.dequeue);
		Log.getInstance().getLog().info("(SEND)   = " + this.sent);
		Log.getInstance().getLog().info("(FAILED) = " + this.failed);
		assert (this.sent + this.failed + Service.getInstance().getMasterQueueLoad() == Limits.NO_OF_MESSAGES);
		assert (this.failed == this.noRouteFailed);
		Service.getInstance().stop();
		Service.getInstance().terminate();
		Log.getInstance().getLog().info("DEQUEUE = " + this.dequeue);
		assert (this.sent + this.failed + this.dequeue == Limits.NO_OF_MESSAGES);
	}
}
