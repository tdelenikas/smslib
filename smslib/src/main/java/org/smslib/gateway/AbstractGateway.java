
package org.smslib.gateway;

import java.util.Random;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.Service;
import org.smslib.core.Capabilities;
import org.smslib.core.Coverage;
import org.smslib.core.CreditBalance;
import org.smslib.core.Statistics;
import org.smslib.message.DeliveryReportMessage.DeliveryStatus;
import org.smslib.message.InboundMessage;
import org.smslib.message.MsIsdn;
import org.smslib.message.OutboundMessage;
import org.smslib.queue.DefaultOutboundQueue;
import org.smslib.queue.IOutboundQueue;
import org.smslib.threading.GatewayMessageDispatcher;

public abstract class AbstractGateway
{
	static Logger logger = LoggerFactory.getLogger(AbstractGateway.class);

	public enum Status
	{
		Starting, Started, Stopping, Stopped, Error
	}

	protected String operatorId = "";

	Status status = Status.Stopped;

	String gatewayId = "";

	MsIsdn senderAddress = new MsIsdn();

	String description = "";

	int priority = 0;

	int maxMessageParts = 1;

	boolean requestDeliveryReport = false;

	Capabilities capabilities = new Capabilities();

	CreditBalance creditBalance = new CreditBalance();

	Statistics statistics = new Statistics();

	Object _LOCK_ = new Object();

	Semaphore concurrency = null;

	IOutboundQueue<OutboundMessage> messageQueue = new DefaultOutboundQueue();

	GatewayMessageDispatcher[] gatewayMessageDispatchers;

	int multipartReferenceNo = 0;

	Random randomizer = new Random();

	public AbstractGateway(int noOfDispatchers, int concurrencyLevel, String id, String description)
	{
		this.gatewayMessageDispatchers = new GatewayMessageDispatcher[noOfDispatchers];
		this.concurrency = new Semaphore(concurrencyLevel, true);
		setGatewayId(id);
		setDescription(description);
	}

	public AbstractGateway(int concurrencyLevel, String id, String description)
	{
		this.gatewayMessageDispatchers = new GatewayMessageDispatcher[concurrencyLevel - 1];
		this.concurrency = new Semaphore(concurrencyLevel, true);
		setGatewayId(id);
		setDescription(description);
	}

	public Status getStatus()
	{
		return this.status;
	}

	public String getGatewayId()
	{
		return this.gatewayId;
	}

	public void setGatewayId(String gatewayId)
	{
		this.gatewayId = gatewayId;
	}

	public MsIsdn getSenderAddress()
	{
		return this.senderAddress;
	}

	public void setSenderAddress(MsIsdn senderAddress)
	{
		this.senderAddress = senderAddress;
	}

	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public int getPriority()
	{
		return this.priority;
	}

	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	public int getMaxMessageParts()
	{
		return this.maxMessageParts;
	}

	public void setMaxMessageParts(int n)
	{
		this.maxMessageParts = n;
	}

	public boolean getRequestDeliveryReport()
	{
		return this.requestDeliveryReport;
	}

	public void setRequestDeliveryReport(boolean requestDeliveryReport)
	{
		this.requestDeliveryReport = requestDeliveryReport;
	}

	public Capabilities getCapabilities()
	{
		return this.capabilities;
	}

	public void setCapabilities(Capabilities capabilities)
	{
		this.capabilities = capabilities;
	}

	public CreditBalance getCreditBalance()
	{
		return this.creditBalance;
	}

	public Statistics getStatistics()
	{
		return this.statistics;
	}

	final public boolean start()
	{
		synchronized (this._LOCK_)
		{
			if ((getStatus() == Status.Stopped) || (getStatus() == Status.Error))
			{
				try
				{
					setStatus(Status.Starting);
					logger.info(String.format("Starting gateway: %s", toShortString()));
					getMessageQueue().start();
					_start();
					for (int i = 0; i < this.gatewayMessageDispatchers.length; i++)
					{
						this.gatewayMessageDispatchers[i] = new GatewayMessageDispatcher(String.format("Gateway Dispatcher %d [%s]", i, getGatewayId()), getMessageQueue(), this);
						this.gatewayMessageDispatchers[i].start();
					}
					setStatus(Status.Started);
				}
				catch (Exception e)
				{
					logger.error("Unhandled Exception!", e);
					try
					{
						stop();
					}
					catch (Exception e1)
					{
						logger.error("Unhandled Exception!", e1);
					}
					setStatus(Status.Error);
				}
			}
		}
		return (getStatus() == Status.Started);
	}

	final public boolean stop()
	{
		synchronized (this._LOCK_)
		{
			if ((getStatus() == Status.Started) || (getStatus() == Status.Error))
			{
				try
				{
					setStatus(Status.Stopping);
					logger.info(String.format("Stopping gateway: %s", toShortString()));
					for (int i = 0; i < this.gatewayMessageDispatchers.length; i++)
					{
						if (this.gatewayMessageDispatchers[i] != null)
						{
							this.gatewayMessageDispatchers[i].cancel();
							this.gatewayMessageDispatchers[i].join();
						}
					}
					while (true)
					{
						OutboundMessage message = getMessageQueue().get();
						if (message == null) break;
						Service.getInstance().queue(message);
					}
					setStatus(Status.Stopped);
					getMessageQueue().stop();
					_stop();
				}
				catch (Exception e)
				{
					logger.error("Unhandled Exception!", e);
					setStatus(Status.Error);
				}
			}
		}
		return (getStatus() == Status.Stopped);
	}

	final public boolean send(OutboundMessage message) throws Exception
	{
		boolean acquiredLock = false;
		try
		{
			if (getStatus() != Status.Started)
			{
				logger.warn("Outbound message routed via non-started gateway: " + message.toShortString() + " (" + getStatus() + ")");
				return false;
			}
			this.concurrency.acquire();
			acquiredLock = true;
			boolean result = _send(message);
			if (result)
			{
				getStatistics().increaseTotalSent();
				Service.getInstance().getStatistics().increaseTotalSent();
			}
			else
			{
				getStatistics().increaseTotalFailed();
				Service.getInstance().getStatistics().increaseTotalFailed();
			}
			return result;
		}
		catch (Exception e)
		{
			getStatistics().increaseTotalFailures();
			Service.getInstance().getStatistics().increaseTotalFailures();
			throw e;
		}
		finally
		{
			if (acquiredLock) this.concurrency.release();
		}
	}

	final public boolean delete(InboundMessage message) throws Exception
	{
		boolean acquiredLock = false;
		try
		{
			if (getStatus() != Status.Started)
			{
				logger.warn("Delete message via non-started gateway: " + message.toShortString() + " (" + getStatus() + ")");
				return false;
			}
			this.concurrency.acquire();
			acquiredLock = true;
			return _delete(message);
		}
		finally
		{
			if (acquiredLock) this.concurrency.release();
		}
	}

	final public DeliveryStatus queryDeliveryStatus(OutboundMessage message) throws Exception
	{
		boolean acquiredLock = false;
		try
		{
			this.concurrency.acquire();
			acquiredLock = true;
			return _queryDeliveryStatus(message.getOperatorMessageIds().get(0));
		}
		finally
		{
			if (acquiredLock) this.concurrency.release();
		}
	}

	final public DeliveryStatus queryDeliveryStatus(String operatorMessageId) throws Exception
	{
		boolean acquiredLock = false;
		try
		{
			this.concurrency.acquire();
			acquiredLock = true;
			return _queryDeliveryStatus(operatorMessageId);
		}
		finally
		{
			if (acquiredLock) this.concurrency.release();
		}
	}

	final public CreditBalance queryCreditBalance() throws Exception
	{
		boolean acquiredLock = false;
		try
		{
			this.concurrency.acquire();
			acquiredLock = true;
			return _queryCreditBalance();
		}
		finally
		{
			if (acquiredLock) this.concurrency.release();
		}
	}

	final public Coverage queryCoverage(Coverage coverage) throws Exception
	{
		boolean acquiredLock = false;
		try
		{
			this.concurrency.acquire();
			acquiredLock = true;
			return _queryCoverage(coverage);
		}
		finally
		{
			if (acquiredLock) this.concurrency.release();
		}
	}

	public boolean queue(OutboundMessage message) throws Exception
	{
		logger.debug("Queue: " + message.toShortString());
		return getMessageQueue().add(message);
	}

	public int getQueueLoad() throws Exception
	{
		return getMessageQueue().size();
	}

	abstract protected void _start() throws Exception;

	abstract protected void _stop() throws Exception;

	abstract protected boolean _send(OutboundMessage message) throws Exception;

	abstract protected boolean _delete(InboundMessage message) throws Exception;

	abstract protected DeliveryStatus _queryDeliveryStatus(String operatorMessageId) throws Exception;

	abstract protected CreditBalance _queryCreditBalance() throws Exception;

	abstract protected Coverage _queryCoverage(Coverage coverage) throws Exception;

	private void setStatus(Status status)
	{
		Status oldStatus = this.status;
		this.status = status;
		Status newStatus = this.status;
		Service.getInstance().getCallbackManager().registerGatewayStatusEvent(this, oldStatus, newStatus);
	}

	protected IOutboundQueue<OutboundMessage> getMessageQueue()
	{
		return this.messageQueue;
	}

	protected int getNextMultipartReferenceNo()
	{
		if (this.multipartReferenceNo == 0)
		{
			this.multipartReferenceNo = this.randomizer.nextInt();
			if (this.multipartReferenceNo < 0) this.multipartReferenceNo *= -1;
			this.multipartReferenceNo %= 65536;
		}
		this.multipartReferenceNo = (this.multipartReferenceNo + 1) % 65536;
		return this.multipartReferenceNo;
	}

	public String toShortString()
	{
		return String.format("%s (%s)", getGatewayId(), getDescription());
	}

	@Override
	public String toString()
	{
		StringBuffer b = new StringBuffer(1024);
		b.append("== GATEWAY ========================================================================%n");
		b.append(String.format("Gateway ID:  %s%n", getGatewayId()));
		b.append(String.format("Description: %s%n", getDescription()));
		b.append(String.format("Sender ID:   %s%n", getSenderAddress()));
		b.append(String.format("-- Capabilities --%n"));
		b.append(getCapabilities().toString());
		b.append(String.format("-- Settings --%n"));
		b.append(String.format("Request Delivery Reports: %b%n", getRequestDeliveryReport()));
		b.append("== GATEWAY END ========================================================================%n");
		return b.toString();
	}
}
