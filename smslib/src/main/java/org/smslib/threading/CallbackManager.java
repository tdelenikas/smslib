
package org.smslib.threading;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.smslib.Service;
import org.smslib.callback.IDeliveryReportCallback;
import org.smslib.callback.IDequeueMessageCallback;
import org.smslib.callback.IGatewayStatusCallback;
import org.smslib.callback.IInboundCallCallback;
import org.smslib.callback.IInboundMessageCallback;
import org.smslib.callback.IMessageSentCallback;
import org.smslib.callback.IQueueThresholdCallback;
import org.smslib.callback.IServiceStatusCallback;
import org.smslib.callback.events.BaseCallbackEvent;
import org.smslib.callback.events.DeliveryReportCallbackEvent;
import org.smslib.callback.events.DequeueMessageCallbackEvent;
import org.smslib.callback.events.GatewayStatusCallbackEvent;
import org.smslib.callback.events.InboundCallEvent;
import org.smslib.callback.events.InboundMessageEvent;
import org.smslib.callback.events.MessageSentCallbackEvent;
import org.smslib.callback.events.QueueThresholdCallbackEvent;
import org.smslib.callback.events.ServiceStatusCallbackEvent;
import org.smslib.core.Settings;
import org.smslib.gateway.AbstractGateway;
import org.smslib.helper.Log;
import org.smslib.message.DeliveryReportMessage;
import org.smslib.message.InboundMessage;
import org.smslib.message.MsIsdn;
import org.smslib.message.OutboundMessage;

public class CallbackManager
{
	LinkedBlockingQueue<BaseCallbackEvent> eventQueue = new LinkedBlockingQueue<BaseCallbackEvent>();

	IServiceStatusCallback serviceStatusCallback = null;

	IGatewayStatusCallback gatewayStatusCallback = null;

	IMessageSentCallback messageSentCallback = null;

	IInboundMessageCallback inboundMessageCallback = null;

	IInboundCallCallback inboundCallCallback = null;

	IDeliveryReportCallback deliveryReportCallback = null;

	IDequeueMessageCallback dequeueMessageCallback = null;

	IQueueThresholdCallback queueThresholdCallback = null;

	CallbackManagerDispatcher dispatcher = null;

	boolean shouldCancel = false;

	public boolean registerServiceStatusEvent(Service.Status oldStatus, Service.Status newStatus)
	{
		return (this.serviceStatusCallback == null ? false : this.eventQueue.add(new ServiceStatusCallbackEvent(oldStatus, newStatus)));
	}

	public boolean registerGatewayStatusEvent(AbstractGateway gateway, AbstractGateway.Status oldStatus, AbstractGateway.Status newStatus)
	{
		return (this.gatewayStatusCallback == null ? false : this.eventQueue.add(new GatewayStatusCallbackEvent(gateway, oldStatus, newStatus)));
	}

	public boolean registerMessageSentEvent(OutboundMessage message)
	{
		return (this.messageSentCallback == null ? false : this.eventQueue.add(new MessageSentCallbackEvent(message)));
	}

	public boolean registerInboundMessageEvent(InboundMessage message)
	{
		return (this.inboundMessageCallback == null ? false : this.eventQueue.add(new InboundMessageEvent(message)));
	}

	public boolean registerInboundCallEvent(MsIsdn msisdn, String gatewayId)
	{
		return (this.inboundCallCallback == null ? false : this.eventQueue.add(new InboundCallEvent(msisdn, gatewayId)));
	}

	public boolean registerDeliveryReportEvent(DeliveryReportMessage message)
	{
		return (this.deliveryReportCallback == null ? false : this.eventQueue.add(new DeliveryReportCallbackEvent(message)));
	}

	public boolean registerDequeueMessageEvent(OutboundMessage message)
	{
		return (this.dequeueMessageCallback == null ? false : this.eventQueue.add(new DequeueMessageCallbackEvent(message)));
	}

	public boolean registerQueueThresholdEvent(int queueLoad)
	{
		return (this.queueThresholdCallback == null ? false : this.eventQueue.add(new QueueThresholdCallbackEvent(queueLoad)));
	}

	public void setServiceStatusCallback(IServiceStatusCallback serviceStatusCallback)
	{
		this.serviceStatusCallback = serviceStatusCallback;
	}

	public void setGatewayStatusCallback(IGatewayStatusCallback gatewayStatusCallback)
	{
		this.gatewayStatusCallback = gatewayStatusCallback;
	}

	public void setMessageSentCallback(IMessageSentCallback messageSentCallback)
	{
		this.messageSentCallback = messageSentCallback;
	}

	public void setInboundMessageCallback(IInboundMessageCallback inboundMessageCallback)
	{
		this.inboundMessageCallback = inboundMessageCallback;
	}

	public void setInboundCallCallback(IInboundCallCallback inboundCallCallback)
	{
		this.inboundCallCallback = inboundCallCallback;
	}

	public void setDeliveryReportCallback(IDeliveryReportCallback deliveryReportCallback)
	{
		this.deliveryReportCallback = deliveryReportCallback;
	}

	public void setDequeueMessageCallback(IDequeueMessageCallback dequeueMessageCallback)
	{
		this.dequeueMessageCallback = dequeueMessageCallback;
	}

	public void setQueueThresholdCallback(IQueueThresholdCallback queueThresholdCallback)
	{
		this.queueThresholdCallback = queueThresholdCallback;
	}

	public int getQueueLoad()
	{
		return this.eventQueue.size();
	}

	public void start()
	{
		Log.getInstance().getLog().debug("Starting...");
		this.dispatcher = new CallbackManagerDispatcher();
		this.dispatcher.start();
	}

	public void stop()
	{
		Log.getInstance().getLog().debug("Cancelling!");
		this.shouldCancel = true;
		this.dispatcher.interrupt();
		try
		{
			this.dispatcher.join();
		}
		catch (InterruptedException e)
		{
			Log.getInstance().getLog().warn("Interrupted!", e);
		}
		this.dispatcher = null;
	}

	public class CallbackManagerDispatcher extends Thread
	{
		public CallbackManagerDispatcher()
		{
			setName("Callback Manager Dispatcher");
			setDaemon(false);
		}

		@Override
		public void run()
		{
			Log.getInstance().getLog().debug("Started!");
			while (!CallbackManager.this.shouldCancel)
			{
				try
				{
					BaseCallbackEvent ev = CallbackManager.this.eventQueue.poll(Settings.callbackDispatcherQueueTimeout, TimeUnit.MILLISECONDS);
					if (ev != null)
					{
						boolean consumed = false;
						boolean handlerFound = false;
						if (ev instanceof ServiceStatusCallbackEvent)
						{
							if (CallbackManager.this.serviceStatusCallback != null)
							{
								try
								{
									handlerFound = true;
									consumed = CallbackManager.this.serviceStatusCallback.process((ServiceStatusCallbackEvent) ev);
								}
								catch (Exception e)
								{
									Log.getInstance().getLog().error("Error in ServiceStatusCallback!", e);
								}
							}
						}
						else if (ev instanceof GatewayStatusCallbackEvent)
						{
							if (CallbackManager.this.gatewayStatusCallback != null)
							{
								try
								{
									handlerFound = true;
									consumed = CallbackManager.this.gatewayStatusCallback.process((GatewayStatusCallbackEvent) ev);
								}
								catch (Exception e)
								{
									Log.getInstance().getLog().error("Error in GatewayStatusCallback!", e);
								}
							}
						}
						else if (ev instanceof MessageSentCallbackEvent)
						{
							if (CallbackManager.this.messageSentCallback != null)
							{
								try
								{
									handlerFound = true;
									consumed = CallbackManager.this.messageSentCallback.process((MessageSentCallbackEvent) ev);
								}
								catch (Exception e)
								{
									Log.getInstance().getLog().error("Error in MessageSentCallback!", e);
								}
							}
						}
						else if (ev instanceof InboundMessageEvent)
						{
							if (CallbackManager.this.inboundMessageCallback != null)
							{
								try
								{
									handlerFound = true;
									consumed = CallbackManager.this.inboundMessageCallback.process((InboundMessageEvent) ev);
									if (consumed && Settings.deleteMessagesAfterCallback) Service.getInstance().delete(((InboundMessageEvent) ev).getMessage());
								}
								catch (Exception e)
								{
									Log.getInstance().getLog().error("Error in InboundMessageCallback!", e);
								}
							}
						}
						else if (ev instanceof InboundCallEvent)
						{
							if (CallbackManager.this.inboundCallCallback != null)
							{
								try
								{
									handlerFound = true;
									consumed = CallbackManager.this.inboundCallCallback.process((InboundCallEvent) ev);
								}
								catch (Exception e)
								{
									Log.getInstance().getLog().error("Error in InboundCallCallback!", e);
								}
							}
						}
						else if (ev instanceof DeliveryReportCallbackEvent)
						{
							if (CallbackManager.this.deliveryReportCallback != null)
							{
								try
								{
									handlerFound = true;
									consumed = CallbackManager.this.deliveryReportCallback.process((DeliveryReportCallbackEvent) ev);
									if (consumed && Settings.deleteMessagesAfterCallback) Service.getInstance().delete(((DeliveryReportCallbackEvent) ev).getMessage());
								}
								catch (Exception e)
								{
									Log.getInstance().getLog().error("Error in DeliveryReportCallback!", e);
								}
							}
						}
						else if (ev instanceof DequeueMessageCallbackEvent)
						{
							if (CallbackManager.this.dequeueMessageCallback != null)
							{
								try
								{
									handlerFound = true;
									consumed = CallbackManager.this.dequeueMessageCallback.process((DequeueMessageCallbackEvent) ev);
								}
								catch (Exception e)
								{
									Log.getInstance().getLog().error("Error in DequeueMessageCallback!", e);
								}
							}
						}
						else if (ev instanceof QueueThresholdCallbackEvent)
						{
							if (CallbackManager.this.queueThresholdCallback != null)
							{
								try
								{
									handlerFound = true;
									consumed = CallbackManager.this.queueThresholdCallback.process((QueueThresholdCallbackEvent) ev);
								}
								catch (Exception e)
								{
									Log.getInstance().getLog().error("Error in QueueThresholdCallback!", e);
								}
							}
						}
						if (handlerFound && !consumed) CallbackManager.this.eventQueue.put(ev);
					}
					sleep(Settings.callbackDispatcherYield);
				}
				catch (InterruptedException e1)
				{
					if (!CallbackManager.this.shouldCancel) Log.getInstance().getLog().error("Interrupted!", e1);
				}
				catch (Exception e2)
				{
					Log.getInstance().getLog().error("Unhandled exception!", e2);
				}
			}
			Log.getInstance().getLog().debug("Stopped!");
		}
	}
}
