
package org.smslib;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.callback.IMessageSentCallback;
import org.smslib.callback.IQueueThresholdCallback;
import org.smslib.callback.events.MessageSentCallbackEvent;
import org.smslib.callback.events.QueueThresholdCallbackEvent;
import org.smslib.core.Capabilities;
import org.smslib.gateway.MockGateway;
import org.smslib.message.OutboundMessage;
import org.smslib.message.OutboundMessage.SentStatus;

public class Test_QueueRouting1 extends TestCase
{
	static Logger logger = LoggerFactory.getLogger(Test_QueueRouting1.class);

	int failed;

	int sent;

	class MessageSentCallback implements IMessageSentCallback
	{
		@Override
		public boolean process(MessageSentCallbackEvent event)
		{
			if (event.getMessage().getSentStatus() == SentStatus.Failed) Test_QueueRouting1.this.failed++;
			else if (event.getMessage().getSentStatus() == SentStatus.Sent) Test_QueueRouting1.this.sent++;
			else throw new RuntimeException("Invalid status -> " + event.getMessage().getSentStatus());
			return true;
		}
	}

	class QueueThresholdCallback implements IQueueThresholdCallback
	{
		@Override
		public boolean process(QueueThresholdCallbackEvent event)
		{
			logger.info("=== Queue threshold reached : " + event.getQueueLoad() + " ===");
			return true;
		}
	}

	public void test() throws Exception
	{
		Service.getInstance().setMessageSentCallback(new MessageSentCallback());
		Service.getInstance().setQueueThresholdCallback(new QueueThresholdCallback());
		Service.getInstance().start();
		Capabilities c = new Capabilities();
		c.set(Capabilities.Caps.CanSendMessage);
		MockGateway g1 = new MockGateway("G1", "Mock Gateway #1", c, 0, 30);
		MockGateway g2 = new MockGateway("G2", "Mock Gateway #2", c, 20, 50);
		MockGateway g3 = new MockGateway("G3", "Mock Gateway #3", c, 40, 70);
		Service.getInstance().registerGateway(g1);
		Service.getInstance().registerGateway(g2);
		Service.getInstance().registerGateway(g3);
		logger.info("Queueing " + Limits.NO_OF_MESSAGES + " messages...");
		for (int i = 0; i < Limits.NO_OF_MESSAGES; i++)
			assert (Service.getInstance().queue(new OutboundMessage("306974000000", "Hello World! (queued)")) == 1);
		while (Service.getInstance().getAllQueueLoad() != 0)
			Thread.sleep(1000);
		logger.info("SENT TRAFFIC");
		logger.info("G1 = " + g1.getStatistics().getTotalSent());
		logger.info("G2 = " + g2.getStatistics().getTotalSent());
		logger.info("G3 = " + g3.getStatistics().getTotalSent());
		logger.info("(SEND)   = " + this.sent);
		logger.info("(FAILED) = " + this.failed);
		assert (g1.getStatistics().getTotalSent() + g2.getStatistics().getTotalSent() + g3.getStatistics().getTotalSent() + this.failed == Limits.NO_OF_MESSAGES);
		assert (this.sent + this.failed == Limits.NO_OF_MESSAGES);
		Service.getInstance().unregisterGateway(g1);
		Service.getInstance().unregisterGateway(g2);
		Service.getInstance().unregisterGateway(g3);
		Service.getInstance().stop();
		Service.getInstance().terminate();
		Service.getInstance().setQueueThresholdCallback(null);
	}
}
