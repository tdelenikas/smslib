
package org.smslib;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.callback.IDequeueMessageCallback;
import org.smslib.callback.IMessageSentCallback;
import org.smslib.callback.events.DequeueMessageCallbackEvent;
import org.smslib.callback.events.MessageSentCallbackEvent;
import org.smslib.core.Capabilities;
import org.smslib.core.Settings;
import org.smslib.gateway.MockGateway;
import org.smslib.message.OutboundMessage;
import org.smslib.message.OutboundMessage.FailureCause;
import org.smslib.message.OutboundMessage.SentStatus;

public class Test_QueueRouting5 extends TestCase
{
	static Logger logger = LoggerFactory.getLogger(Test_QueueRouting5.class);

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
		logger.info("Queueing " + Limits.NO_OF_MESSAGES + " messages...");
		for (int i = 0; i < Limits.NO_OF_MESSAGES; i++)
			assert (Service.getInstance().queue(new OutboundMessage("306974000000", "Hello World! (queued)")) == 1);
		Thread.sleep(1000);
		logger.info("QUEUE LOAD (MASTER) = " + Service.getInstance().getMasterQueueLoad());
		if (Settings.keepOutboundMessagesInQueue) assert (Limits.NO_OF_MESSAGES == Service.getInstance().getMasterQueueLoad());
		for (int i = 0; i < this.GATEWAY_COUNT; i++)
		{
			if (i >= 3) g[i] = new MockGateway("G" + i, "Mock Gateway #" + i, c, 0, 100);
			else g[i] = new MockGateway("G" + i, "Mock Gateway #" + i, c, 10, 100);
			Service.getInstance().registerGateway(g[i]);
		}
		logger.info("Sleeping for a while...");
		Thread.sleep(10000);
		logger.info("Now shutdown all gateways!");
		int grandTotal = 0;
		for (int i = 0; i < this.GATEWAY_COUNT; i++)
		{
			Service.getInstance().unregisterGateway(g[i]);
			grandTotal += g[i].getStatistics().getTotalSent();
		}
		Thread.sleep(1000);
		logger.info("SENT TRAFFIC");
		logger.info("G-TOTAL = " + grandTotal);
		logger.info("SENT = " + this.sent);
		logger.info("FAILED = " + this.failed);
		logger.info("QUEUE LOAD (ALL) = " + Service.getInstance().getAllQueueLoad());
		logger.info("QUEUE LOAD (MASTER) = " + Service.getInstance().getMasterQueueLoad());
		logger.info("GATEWAYS' LIST = " + Service.getInstance().getGatewayIDs().size());
		logger.info("DEQUEUE = " + this.dequeue);
		logger.info("(SEND)   = " + this.sent);
		logger.info("(FAILED) = " + this.failed);
		assert (this.sent + this.failed + Service.getInstance().getMasterQueueLoad() == Limits.NO_OF_MESSAGES);
		assert (this.failed == this.noRouteFailed);
		Service.getInstance().stop();
		Service.getInstance().terminate();
		logger.info("DEQUEUE = " + this.dequeue);
		assert (this.sent + this.failed + this.dequeue == Limits.NO_OF_MESSAGES);
	}
}
