
package org.smslib;

import java.util.Date;
import java.util.Random;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.callback.IMessageSentCallback;
import org.smslib.callback.events.MessageSentCallbackEvent;
import org.smslib.core.Capabilities;
import org.smslib.gateway.AbstractGateway.Status;
import org.smslib.gateway.MockGateway;
import org.smslib.message.OutboundMessage;
import org.smslib.message.OutboundMessage.SentStatus;

public class Test_QueueRouting4 extends TestCase
{
	static Logger logger = LoggerFactory.getLogger(Test_QueueRouting4.class);

	int GATEWAY_COUNT = 10;

	int failed = 0;

	int sent = 0;

	class MessageSentCallback implements IMessageSentCallback
	{
		@Override
		public boolean process(MessageSentCallbackEvent event)
		{
			if (event.getMessage().getSentStatus() == SentStatus.Failed) Test_QueueRouting4.this.failed++;
			else if (event.getMessage().getSentStatus() == SentStatus.Sent) Test_QueueRouting4.this.sent++;
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
		logger.info("Queueing " + Limits.NO_OF_MESSAGES + " messages...");
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
				Service.getInstance().unregisterGateway(g[whichGateway]);
				logger.info("Out of pool : #" + whichGateway);
			}
			else
			{
				Service.getInstance().registerGateway(g[whichGateway]);
				logger.info("In pool : #" + whichGateway);
			}
			Thread.sleep(1000);
		}
		while (Service.getInstance().getAllQueueLoad() != 0)
			Thread.sleep(1000);
		Thread.sleep(1000);
		logger.info("SENT TRAFFIC");
		int grandTotal = 0;
		for (int i = 0; i < this.GATEWAY_COUNT; i++)
		{
			logger.info("G" + i + " = " + g[i].getStatistics().getTotalSent());
			grandTotal += g[i].getStatistics().getTotalSent();
		}
		logger.info("G-TOTAL  = " + grandTotal);
		logger.info("(SEND)   = " + this.sent);
		logger.info("(FAILED) = " + this.failed);
		assert (grandTotal + this.failed == Limits.NO_OF_MESSAGES);
		assert (this.sent + this.failed == Limits.NO_OF_MESSAGES);
		for (int i = 0; i < this.GATEWAY_COUNT; i++)
			Service.getInstance().unregisterGateway(g[i]);
		Service.getInstance().stop();
		Service.getInstance().terminate();
	}
}
