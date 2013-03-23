package org.smslib.gateway;

import java.io.IOException;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
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
	int delay = 100;
	int failureRate = 10;

	public MockGateway(String gatewayId, String... parms)
	{
		super(1, 1, gatewayId, "Mock Gateway");
		Capabilities caps = new Capabilities();
		caps.set(Caps.CanSendMessage);
		setCapabilities(caps);
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
		//if (failOperation()) throw new IOException("Dummy Failure!");
		//Nothing here...
	}

	@Override
	protected void _stop() throws IOException
	{
		//if (failOperation()) throw new IOException("Dummy Failure!");
		//Nothing here...
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
