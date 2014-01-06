
package org.smslib.threading;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.Service;
import org.smslib.core.Settings;
import org.smslib.gateway.AbstractGateway;
import org.smslib.gateway.AbstractGateway.Status;
import org.smslib.helper.Common;
import org.smslib.message.OutboundMessage;
import org.smslib.message.OutboundMessage.FailureCause;
import org.smslib.message.OutboundMessage.SentStatus;
import org.smslib.queue.IOutboundQueue;

public class ServiceMessageDispatcher extends Thread
{
	static Logger logger = LoggerFactory.getLogger(ServiceMessageDispatcher.class);

	boolean shouldCancel = false;

	IOutboundQueue<OutboundMessage> messageQueue;

	public ServiceMessageDispatcher(String name, IOutboundQueue<OutboundMessage> messageQueue)
	{
		setName(name);
		setDaemon(false);
		this.messageQueue = messageQueue;
	}

	@Override
	public void run()
	{
		logger.debug("Started!");
		while (!this.shouldCancel)
		{
			try
			{
				boolean pollNow = true;
				if (Settings.keepOutboundMessagesInQueue)
				{
					if (getNoOfStartedGateways() == 0)
					{
						pollNow = false;
						Thread.sleep(1000);
					}
				}
				if (pollNow)
				{
					OutboundMessage message = this.messageQueue.get(Settings.serviceDispatcherQueueTimeout, TimeUnit.MILLISECONDS);
					if (message != null)
					{
						Collection<AbstractGateway> routes = Service.getInstance().routeMessage(message);
						if (routes.isEmpty())
						{
							message.setSentStatus(SentStatus.Failed);
							message.setFailureCause(FailureCause.NoRoute);
							Service.getInstance().getCallbackManager().registerMessageSentEvent(message);
						}
						else
						{
							message.setRoutingTable(new LinkedList<>(routes));
							logger.debug("Routing table: " + Common.dumpRoutingTable(message));
							message.getRoutingTable().get(0).queue(message);
						}
					}
					sleep(Settings.serviceDispatcherYield);
				}
			}
			catch (InterruptedException e1)
			{
				if (!this.shouldCancel) logger.error("Interrupted!", e1);
			}
			catch (Exception e)
			{
				logger.error("Unhandled exception!", e);
			}
		}
		logger.debug("Stopped!");
	}

	public void cancel()
	{
		logger.debug("Cancelling!");
		this.shouldCancel = true;
	}

	private int getNoOfStartedGateways()
	{
		try
		{
			int count = 0;
			for (String gatewayId : Service.getInstance().getGatewayIDs())
			{
				if (Service.getInstance().getGatewayById(gatewayId).getStatus() == Status.Started) count++;
			}
			return count;
		}
		catch (Exception e)
		{
			logger.warn("Gateway list modified, re-testing...", e);
			return getNoOfStartedGateways();
		}
	}
}
