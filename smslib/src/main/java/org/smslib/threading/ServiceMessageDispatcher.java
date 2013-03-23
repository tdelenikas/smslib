
package org.smslib.threading;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.smslib.Service;
import org.smslib.core.Settings;
import org.smslib.gateway.AbstractGateway;
import org.smslib.gateway.AbstractGateway.Status;
import org.smslib.helper.Common;
import org.smslib.helper.Log;
import org.smslib.message.OutboundMessage;
import org.smslib.message.OutboundMessage.FailureCause;
import org.smslib.message.OutboundMessage.SentStatus;

public class ServiceMessageDispatcher extends Thread
{
	boolean shouldCancel = false;

	PriorityBlockingQueue<OutboundMessage> messageQueue;

	public ServiceMessageDispatcher(String name, PriorityBlockingQueue<OutboundMessage> messageQueue)
	{
		setName(name);
		setDaemon(false);
		this.messageQueue = messageQueue;
	}

	@Override
	public void run()
	{
		Log.getInstance().getLog().debug("Started!");
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
					OutboundMessage message = this.messageQueue.poll(Settings.serviceDispatcherQueueTimeout, TimeUnit.MILLISECONDS);
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
							message.setRoutingTable(new LinkedList<AbstractGateway>(routes));
							Log.getInstance().getLog().debug("Routing table: " + Common.dumpRoutingTable(message));
							message.getRoutingTable().get(0).queue(message);
						}
					}
					sleep(Settings.serviceDispatcherYield);
				}
			}
			catch (InterruptedException e1)
			{
				if (!this.shouldCancel) Log.getInstance().getLog().error("Interrupted!", e1);
			}
			catch (Exception e)
			{
				Log.getInstance().getLog().error("Unhandled exception!", e);
			}
		}
		Log.getInstance().getLog().debug("Stopped!");
	}

	public void cancel()
	{
		Log.getInstance().getLog().debug("Cancelling!");
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
			Log.getInstance().getLog().warn("Gateway list modified, re-testing...", e);
			return getNoOfStartedGateways();
		}
	}
}
