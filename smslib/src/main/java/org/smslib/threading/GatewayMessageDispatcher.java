
package org.smslib.threading;

import java.util.concurrent.TimeUnit;
import org.smslib.Service;
import org.smslib.core.Settings;
import org.smslib.gateway.AbstractGateway;
import org.smslib.gateway.AbstractGateway.Status;
import org.smslib.helper.Log;
import org.smslib.message.OutboundMessage;
import org.smslib.message.OutboundMessage.FailureCause;
import org.smslib.message.OutboundMessage.SentStatus;
import org.smslib.queue.IOutboundQueue;

public class GatewayMessageDispatcher extends Thread
{
	boolean shouldCancel = false;

	IOutboundQueue<OutboundMessage> messageQueue;

	AbstractGateway gateway;

	public GatewayMessageDispatcher(String name, IOutboundQueue<OutboundMessage> messageQueue, AbstractGateway gateway)
	{
		setName(name);
		setDaemon(false);
		this.messageQueue = messageQueue;
		this.gateway = gateway;
	}

	@Override
	public void run()
	{
		Log.getInstance().getLog().debug("Started!");
		while (!this.shouldCancel)
		{
			try
			{
				OutboundMessage message = this.messageQueue.get(Settings.gatewayDispatcherQueueTimeout, TimeUnit.MILLISECONDS);
				if (message != null)
				{
					boolean sendOk;
					try
					{
						sendOk = this.gateway.send(message);
					}
					catch (Exception e)
					{
						Log.getInstance().getLog().error("Send failed!", e);
						sendOk = false;
					}
					if (sendOk)
					{
						Service.getInstance().getCallbackManager().registerMessageSentEvent(message);
					}
					else
					{
						message.getRoutingTable().removeFirst();
						if (message.getRoutingTable().size() > 0)
						{
							Log.getInstance().getLog().debug("Re-routing message to : " + message.getRoutingTable().get(0).getGatewayId());
							if (message.getRoutingTable().get(0).getStatus() != Status.Started)
							{
								Log.getInstance().getLog().debug("Routing path contains inactive gateway, re-routing to main queue...");
								Service.getInstance().queue(message);
							}
							else message.getRoutingTable().get(0).queue(message);
						}
						else
						{
							message.setSentStatus(SentStatus.Failed);
							message.setFailureCause(FailureCause.NoRoute);
							Service.getInstance().getCallbackManager().registerMessageSentEvent(message);
						}
					}
				}
				sleep(Settings.gatewayDispatcherYield);
			}
			catch (InterruptedException e1)
			{
				if (!this.shouldCancel) Log.getInstance().getLog().error("Interrupted!", e1);
			}
			catch (Exception e1)
			{
				Log.getInstance().getLog().error("Unhandled exception!", e1);
			}
		}
		Log.getInstance().getLog().debug("Stopped!");
	}

	public void cancel()
	{
		Log.getInstance().getLog().debug("Cancelling!");
		this.shouldCancel = true;
	}
}
