
package org.smslib.gateway;

import java.io.IOException;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.smslib.Service;
import org.smslib.core.Capabilities;
import org.smslib.core.Capabilities.Caps;
import org.smslib.core.Coverage;
import org.smslib.core.CreditBalance;
import org.smslib.message.DeliveryReportMessage.DeliveryStatus;
import org.smslib.message.InboundMessage;
import org.smslib.message.OutboundMessage;
import org.smslib.message.OutboundMessage.FailureCause;
import org.smslib.message.OutboundMessage.SentStatus;

public class MockGateway extends AbstractGateway
{
	private Thread thread;

	int delay;

	int failureRate;

	protected boolean running = true;

	public MockGateway(String gatewayId, String... parms)
	{
		this(gatewayId, 100, 10);
	}

	public MockGateway(String id, int delay, int failureRate)
	{
		super(1, 1, id, "Mock Gateway");
		Capabilities caps = new Capabilities();
		caps.set(Caps.CanSendMessage);
		setCapabilities(caps);
		this.failureRate = failureRate;
		this.delay = delay;
	}

	public MockGateway(String id, String description, Capabilities caps, int failureRate, int delay)
	{
		super(1, 1, id, description);
		setCapabilities(caps);
		this.failureRate = failureRate;
		this.delay = delay;
	}

	public MockGateway(String id, String description, Capabilities caps, int noOfDispatchers, int concurrencyLevel, int failureRate, int delay)
	{
		super(noOfDispatchers, concurrencyLevel, id, description);
		setCapabilities(caps);
		this.failureRate = failureRate;
		this.delay = delay;
	}

	@Override
	public boolean _send(OutboundMessage message) throws IOException
	{
		boolean shouldFail = failOperation();
		try
		{
			Thread.sleep(this.delay);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		if (!shouldFail)
		{
			message.setGatewayId(this.getGatewayId());
			message.setSentDate(new Date());
			message.getOperatorMessageIds().add(UUID.randomUUID().toString());
			message.setSentStatus(SentStatus.Sent);
			message.setFailureCause(FailureCause.None);
		}
		else
		{
			message.setSentStatus(SentStatus.Failed);
			message.setFailureCause(FailureCause.GatewayFailure);
		}
		if (failOperation()) throw new IOException("Dummy Failure!");
		return (!shouldFail);
	}

	@Override
	protected void _start() throws IOException
	{
		thread = new Thread(new Runnable()
		{
			private AtomicInteger counter = new AtomicInteger(0);

			@Override
			public void run()
			{
				while (running)
				{
					try
					{
						Thread.sleep(delay);
						int count = counter.incrementAndGet();
						if (failOperation()) throw new IOException("Dummy Failure!");
						InboundMessage message = new InboundMessage("Mock Originator", "Dummy Text! " + count, new Date(), getClass().getName(), count);
						message.setGatewayId(getGatewayId());
						Service.getInstance().getCallbackManager().registerInboundMessageEvent(message);
						break;
					}
					catch (Exception e)
					{
						logger.warn("Error!", e);
						//running = false;
					}
				}
			}
		});
		thread.setDaemon(true);
		thread.setName("MockGateway Thread");
		thread.start();
	}

	@Override
	protected void _stop() throws IOException
	{
		running = false;
		thread.interrupt();
	}

	@Override
	protected DeliveryStatus _queryDeliveryStatus(String operatorMessageId) throws IOException
	{
		if (failOperation()) throw new IOException("Dummy Failure!");
		return null;
	}

	@Override
	protected CreditBalance _queryCreditBalance() throws IOException
	{
		if (failOperation()) throw new IOException("Dummy Failure!");
		return null;
	}

	@Override
	protected Coverage _queryCoverage(Coverage coverage) throws IOException
	{
		if (failOperation()) throw new IOException("Dummy Failure!");
		return null;
	}

	@Override
	protected boolean _delete(InboundMessage message) throws IOException
	{
		if (failOperation()) throw new IOException("Dummy Failure!");
		return false;
	}

	private boolean failOperation()
	{
		boolean shouldFail = false;
		if (this.failureRate >= 100) shouldFail = true;
		else if (this.failureRate != 0)
		{
			Random r = new Random();
			shouldFail = (r.nextInt(100) < this.failureRate);
		}
		return shouldFail;
	}
}
