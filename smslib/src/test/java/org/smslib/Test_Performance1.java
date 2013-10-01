
package org.smslib;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.callback.IMessageSentCallback;
import org.smslib.callback.events.MessageSentCallbackEvent;
import org.smslib.core.Capabilities;
import org.smslib.gateway.AbstractGateway;
import org.smslib.gateway.MockGateway;
import org.smslib.message.OutboundMessage;
import org.smslib.message.OutboundMessage.SentStatus;

public class Test_Performance1 extends TestCase
{
	static Logger logger = LoggerFactory.getLogger(Test_Performance1.class);

	int failed;

	int sent;

	class MessageSentCallback implements IMessageSentCallback
	{
		@Override
		public boolean process(MessageSentCallbackEvent event)
		{
			if (event.getMessage().getSentStatus() == SentStatus.Failed) Test_Performance1.this.failed++;
			else if (event.getMessage().getSentStatus() == SentStatus.Sent) Test_Performance1.this.sent++;
			else throw new RuntimeException("Invalid status -> " + event.getMessage().getSentStatus());
			return true;
		}
	}

	public void test() throws Exception
	{
		int DELAY = 10;
		Service.getInstance().setMessageSentCallback(new MessageSentCallback());
		{
			Service.getInstance().start();
			for (int i = 0; i < 5; i++)
			{
				for (int j = 1; j <= 5; j++)
				{
					RunWith(j, Limits.NO_OF_MESSAGES, DELAY, 0);
					RunWith(j, Limits.NO_OF_MESSAGES, DELAY, (8 * i));
				}
			}
			Service.getInstance().stop();
			Service.getInstance().terminate();
		}
	}

	public void RunWith(int noOfGateways, int noOfMessages, int delay, int failureRate) throws Exception
	{
		int totalSent = 0;
		this.failed = 0;
		this.sent = 0;
		Capabilities c = new Capabilities();
		c.set(Capabilities.Caps.CanSendMessage);
		AbstractGateway[] g = new AbstractGateway[noOfGateways];
		for (int i = 0; i < noOfGateways; i++)
		{
			g[i] = new MockGateway("G" + i, "Mock Gateway #" + i, c, failureRate, delay);
			Service.getInstance().registerGateway(g[i]);
		}
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < noOfMessages; i++)
			Service.getInstance().queue(new OutboundMessage("1", "dummy"));
		while (Service.getInstance().getAllQueueLoad() > 0 || Service.getInstance().getCallbackManager().getQueueLoad() > 0)
		{
			logger.info("QUEUE LOAD: " + Service.getInstance().getAllQueueLoad());
			logger.info("CALLBACK LOAD: " + Service.getInstance().getCallbackManager().getQueueLoad());
			Thread.sleep(500);
		}
		long stopTime = System.currentTimeMillis();
		for (int i = 0; i < noOfGateways; i++)
		{
			Service.getInstance().unregisterGateway(g[i]);
			totalSent += g[i].getStatistics().getTotalSent();
		}
		logger.info(String.format("#%2d / %d%% : %d, %d, %d [%d], < %d >", noOfGateways, failureRate, noOfMessages, this.failed, totalSent, this.sent, (stopTime - startTime)));
		assert (this.sent == totalSent);
		assert ((totalSent + this.failed) == noOfMessages);
	}
}
