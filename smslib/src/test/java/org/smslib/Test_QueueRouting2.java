
package org.smslib;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.callback.IMessageSentCallback;
import org.smslib.callback.events.MessageSentCallbackEvent;
import org.smslib.core.Capabilities;
import org.smslib.gateway.MockGateway;
import org.smslib.message.OutboundMessage;
import org.smslib.message.OutboundMessage.SentStatus;

public class Test_QueueRouting2 extends TestCase
{
	static Logger logger = LoggerFactory.getLogger(Test_QueueRouting2.class);

	int failed;

	int sent;

	class MessageSentCallback implements IMessageSentCallback
	{
		@Override
		public boolean process(MessageSentCallbackEvent event)
		{
			if (event.getMessage().getSentStatus() == SentStatus.Failed) Test_QueueRouting2.this.failed++;
			else if (event.getMessage().getSentStatus() == SentStatus.Sent) Test_QueueRouting2.this.sent++;
			else throw new RuntimeException("Invalid status -> " + event.getMessage().getSentStatus());
			return true;
		}
	}

	public void test() throws Exception
	{
		Service.getInstance().setMessageSentCallback(new MessageSentCallback());
		this.failed = 0;
		this.sent = 0;
		Service.getInstance().start();
		Capabilities c = new Capabilities();
		c.set(Capabilities.Caps.CanSendMessage);
		MockGateway g1 = new MockGateway("G1", "Mock Gateway #1", c, 0, 10);
		MockGateway g2 = new MockGateway("G2", "Mock Gateway #2", c, 20, 10);
		MockGateway g3 = new MockGateway("G3", "Mock Gateway #3", c, 40, 10);
		Service.getInstance().registerGateway(g1);
		Service.getInstance().registerGateway(g2);
		Service.getInstance().registerGateway(g3);
		logger.info("QUEUE TOTALS");
		logger.info("ALL = " + Service.getInstance().getMasterQueueLoad());
		logger.info("G1 = " + Service.getInstance().getGatewayQueueLoad(g1));
		logger.info("G2 = " + Service.getInstance().getGatewayQueueLoad(g2));
		logger.info("G3 = " + Service.getInstance().getGatewayQueueLoad(g3));
		logger.info("Queueing " + Limits.NO_OF_MESSAGES + " messages...");
		for (int i = 0; i < Limits.NO_OF_MESSAGES; i++)
			assert (Service.getInstance().queue(new OutboundMessage("306974000000", "Hello World! (queued)")) == 1);
		logger.info("QUEUE TOTALS");
		logger.info("ALL = " + Service.getInstance().getMasterQueueLoad());
		logger.info("G1 = " + Service.getInstance().getGatewayQueueLoad(g1));
		logger.info("G2 = " + Service.getInstance().getGatewayQueueLoad(g2));
		logger.info("G3 = " + Service.getInstance().getGatewayQueueLoad(g3));
		Thread.sleep(1000);
		logger.info("QUEUE TOTALS");
		logger.info("ALL = " + Service.getInstance().getMasterQueueLoad());
		logger.info("G1 = " + Service.getInstance().getGatewayQueueLoad(g1));
		logger.info("G2 = " + Service.getInstance().getGatewayQueueLoad(g2));
		logger.info("G3 = " + Service.getInstance().getGatewayQueueLoad(g3));
		while (Service.getInstance().getAllQueueLoad() > 0)
			Thread.sleep(1000);
		while (Service.getInstance().getCallbackManager().getQueueLoad() > 0)
			Thread.sleep(1000);
		Thread.sleep(1000);
		logger.info("QUEUE TOTALS");
		logger.info("ALL = " + Service.getInstance().getMasterQueueLoad());
		logger.info("G1 = " + Service.getInstance().getGatewayQueueLoad(g1));
		logger.info("G2 = " + Service.getInstance().getGatewayQueueLoad(g2));
		logger.info("G3 = " + Service.getInstance().getGatewayQueueLoad(g3));
		logger.info("SENT TRAFFIC");
		logger.info("G1 = " + g1.getStatistics().getTotalSent());
		logger.info("G2 = " + g2.getStatistics().getTotalSent());
		logger.info("G3 = " + g3.getStatistics().getTotalSent());
		logger.info("(SEND)   = " + this.sent);
		logger.info("(FAILED) = " + this.failed);
		assert (g1.getStatistics().getTotalSent() + g2.getStatistics().getTotalSent() + g3.getStatistics().getTotalSent() + this.failed == Limits.NO_OF_MESSAGES);
		assert (g1.getStatistics().getTotalSent() + g2.getStatistics().getTotalSent() + g3.getStatistics().getTotalSent() == this.sent);
		assert (this.sent + this.failed == Limits.NO_OF_MESSAGES);
		Service.getInstance().unregisterGateway(g1);
		Service.getInstance().unregisterGateway(g2);
		Service.getInstance().unregisterGateway(g3);
		Service.getInstance().stop();
		Service.getInstance().terminate();
	}
}
