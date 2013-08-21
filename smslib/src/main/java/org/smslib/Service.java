// SMSLib for Java v4
// A universal API for sms messaging.
//
// Copyright (C) 2002-2013, smslib.org
// For more information, visit http://smslib.org
// SMSLib is distributed under the terms of the Apache License version 2.0
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package org.smslib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedList;
import org.smslib.callback.IDeliveryReportCallback;
import org.smslib.callback.IDequeueMessageCallback;
import org.smslib.callback.IGatewayStatusCallback;
import org.smslib.callback.IInboundCallCallback;
import org.smslib.callback.IInboundMessageCallback;
import org.smslib.callback.IMessageSentCallback;
import org.smslib.callback.IQueueThresholdCallback;
import org.smslib.callback.IServiceStatusCallback;
import org.smslib.core.Coverage;
import org.smslib.core.CreditBalance;
import org.smslib.core.Settings;
import org.smslib.core.Statistics;
import org.smslib.crypto.KeyManager;
import org.smslib.gateway.AbstractGateway;
import org.smslib.groups.Group;
import org.smslib.groups.GroupManager;
import org.smslib.helper.Common;
import org.smslib.helper.Log;
import org.smslib.hook.IPreQueueHook;
import org.smslib.hook.IPreSendHook;
import org.smslib.hook.IRouteHook;
import org.smslib.http.HttpServer;
import org.smslib.http.IHttpRequestHandler;
import org.smslib.message.DeliveryReportMessage.DeliveryStatus;
import org.smslib.message.InboundMessage;
import org.smslib.message.MsIsdn;
import org.smslib.message.OutboundMessage;
import org.smslib.message.OutboundMessage.FailureCause;
import org.smslib.message.OutboundMessage.SentStatus;
import org.smslib.queue.DefaultOutboundQueue;
import org.smslib.queue.IOutboundQueue;
import org.smslib.routing.AbstractBalancer;
import org.smslib.routing.AbstractRouter;
import org.smslib.routing.DefaultBalancer;
import org.smslib.routing.DefaultRouter;
import org.smslib.threading.CallbackManager;
import org.smslib.threading.ServiceMessageDispatcher;

public class Service
{
	private static final Service _instance = new Service();

	KeyManager keyManager = new KeyManager();

	IOutboundQueue<OutboundMessage> messageQueue = new DefaultOutboundQueue();

	HashMap<String, AbstractGateway> gateways = new HashMap<String, AbstractGateway>();

	AbstractRouter router = new DefaultRouter();

	AbstractBalancer balancer = new DefaultBalancer();

	IRouteHook routeHook = null;

	IPreSendHook preSendHook = null;

	IPreQueueHook preQueueHook = null;

	HttpServer httpServer = new HttpServer();

	HashMap<String, IHttpRequestHandler> httpRequestHandlers = new HashMap<String, IHttpRequestHandler>();

	Object _LOCK_ = new Object();

	ServiceMessageDispatcher serviceMessageDispatcher = null;

	CallbackManager callbackManager = new CallbackManager();

	GroupManager groupManager = new GroupManager();

	Statistics statistics = new Statistics();

	Status status = Status.Stopped;

	public enum Status
	{
		Starting, Started, Stopping, Stopped, Terminated
	}

	private Service()
	{
		initialize();
	}

	public static Service getInstance()
	{
		return _instance;
	}

	private void initialize()
	{
		try
		{
			Settings.loadSettings();
			this.httpServer.start();
			getCallbackManager().start();
		}
		catch (Exception e)
		{
			throw new RuntimeException("Fatal error in constructor!", e);
		}
	}

	public boolean start()
	{
		boolean allStarted = true;
		synchronized (this._LOCK_)
		{
			if (getStatus() == Status.Stopped)
			{
				setStatus(Status.Starting);
				Log.getInstance().getLog().info("Service starting...");
				for (AbstractGateway gateway : this.gateways.values())
				{
					Log.getInstance().getLog().info("Starting gateway: " + gateway.getGatewayId());
					allStarted &= gateway.start();
				}
				this.serviceMessageDispatcher = new ServiceMessageDispatcher("Main Dispatcher", this.messageQueue);
				this.serviceMessageDispatcher.start();
				setStatus(Status.Started);
				if (allStarted) Log.getInstance().getLog().info("Service started.");
				else Log.getInstance().getLog().warn("Service started, but some gateways did not start!");
			}
		}
		return allStarted;
	}

	public boolean stop()
	{
		boolean allStopped = true;
		synchronized (this._LOCK_)
		{
			if (getStatus() == Status.Started)
			{
				setStatus(Status.Stopping);
				Log.getInstance().getLog().info("Service stopping...");
				for (AbstractGateway gateway : this.gateways.values())
				{
					Log.getInstance().getLog().info("Stopping gateway: " + gateway.getGatewayId());
					allStopped &= gateway.stop();
				}
				this.serviceMessageDispatcher.cancel();
				try
				{
					this.serviceMessageDispatcher.join();
				}
				catch (InterruptedException e)
				{
					Log.getInstance().getLog().error("Unhandled exception!", e);
				}
				setStatus(Status.Stopped);
				if (allStopped) Log.getInstance().getLog().info("Service stopped.");
				else Log.getInstance().getLog().warn("Service stopped, but some gateways did not stop!");
			}
		}
		return allStopped;
	}

	public boolean terminate()
	{
		try
		{
			synchronized (this._LOCK_)
			{
				if (getStatus() == Status.Stopped)
				{
					setStatus(Status.Terminated);
					this.httpServer.terminate();
					DequeueMasterQueue();
					while (getCallbackManager().getQueueLoad() > 0)
					{
						Log.getInstance().getLog().info("Callback queue not empty, waiting...");
						Common.countSheeps(5000);
					}
					getCallbackManager().stop();
					Log.getInstance().getLog().info("Service terminated.");
				}
			}
			return true;
		}
		catch (Exception e)
		{
			Log.getInstance().getLog().error("Unhandled exception!", e);
			return false;
		}
	}

	private void DequeueMasterQueue()
	{
		while (true)
		{
			OutboundMessage message = null;
			try
			{
				message = this.messageQueue.get();
			}
			catch (Exception e)
			{
				Log.getInstance().getLog().error("Unhandled exception!", e);
				Common.countSheeps(1000);
			}
			if (message == null) break;
			getCallbackManager().registerDequeueMessageEvent(message);
		}
	}

	public void setRouteHook(IRouteHook routeHook)
	{
		this.routeHook = routeHook;
	}

	public IRouteHook getRouteHook()
	{
		return this.routeHook;
	}

	public void setPreSendHook(IPreSendHook preSendHook)
	{
		this.preSendHook = preSendHook;
	}

	public IPreSendHook getPreSendHook()
	{
		return this.preSendHook;
	}

	public void setPreQueueHook(IPreQueueHook preQueueHook)
	{
		this.preQueueHook = preQueueHook;
	}

	public IPreQueueHook getPreQueueHook()
	{
		return this.preQueueHook;
	}

	public void setServiceStatusCallback(IServiceStatusCallback serviceStatusCallback)
	{
		getCallbackManager().setServiceStatusCallback(serviceStatusCallback);
	}

	public void setGatewayStatusCallback(IGatewayStatusCallback gatewayStatusCallback)
	{
		getCallbackManager().setGatewayStatusCallback(gatewayStatusCallback);
	}

	public void setMessageSentCallback(IMessageSentCallback messageSentCallback)
	{
		getCallbackManager().setMessageSentCallback(messageSentCallback);
	}

	public void setInboundMessageCallback(IInboundMessageCallback inboundMessageCallback)
	{
		getCallbackManager().setInboundMessageCallback(inboundMessageCallback);
	}

	public void setInboundCallCallback(IInboundCallCallback inboundCallCallback)
	{
		getCallbackManager().setInboundCallCallback(inboundCallCallback);
	}

	public void setDeliveryReportCallback(IDeliveryReportCallback deliveryStatusCallback)
	{
		getCallbackManager().setDeliveryReportCallback(deliveryStatusCallback);
	}

	public void setDequeueMessageCallback(IDequeueMessageCallback dequeueMessageCallback)
	{
		getCallbackManager().setDequeueMessageCallback(dequeueMessageCallback);
	}

	public void setQueueThresholdCallback(IQueueThresholdCallback queueThresholdCallback)
	{
		getCallbackManager().setQueueThresholdCallback(queueThresholdCallback);
	}

	public void addGroup(Group group)
	{
		getGroupManager().addGroup(group);
	}

	public void removeGroup(Group group)
	{
		getGroupManager().removeGroup(group);
	}

	public void removeGroup(String groupId)
	{
		getGroupManager().removeGroup(groupId);
	}

	public boolean registerHttpRequestHandler(String path, IHttpRequestHandler handler)
	{
		if (this.httpRequestHandlers.get(path) != null) return false;
		Log.getInstance().getLog().info("Registering HTTP Request Handler for '" + path + "'");
		this.httpRequestHandlers.put(path, handler);
		return true;
	}

	public int queue(OutboundMessage message) throws Exception
	{
		int messageCount = 0;
		if ((getPreQueueHook() != null) && !getPreQueueHook().process(message)) return 0;
		LinkedList<OutboundMessage> messageList = distributeToGroup(message);
		for (OutboundMessage m : messageList)
		{
			Log.getInstance().getLog().debug("Queued: " + message.toShortString());
			if (this.messageQueue.add(m)) messageCount++;
		}
		return messageCount;
	}

	public int getMasterQueueLoad() throws Exception
	{
		return this.messageQueue.size();
	}

	public int getGatewayQueueLoad(AbstractGateway gateway) throws Exception
	{
		return (gateway.getQueueLoad());
	}

	public int getAllQueueLoad() throws Exception
	{
		int total = 0;
		for (AbstractGateway g : getGateways().values())
			total += g.getQueueLoad();
		total += getMasterQueueLoad();
		return total;
	}

	public boolean send(OutboundMessage message)
	{
		Log.getInstance().getLog().debug("Send: " + message.toShortString());
		if (getStatus() == Status.Started)
		{
			if ((getPreSendHook() != null) && !getPreSendHook().process(message))
			{
				message.setSentStatus(SentStatus.Failed);
				message.setFailureCause(FailureCause.Cancelled);
				return false;
			}
			Collection<AbstractGateway> routes = routeMessage(message);
			if (!routes.isEmpty())
			{
				LinkedList<AbstractGateway> selectedGateways = new LinkedList<AbstractGateway>(routes);
				if (!selectedGateways.isEmpty())
				{
					for (int i = 0; i < selectedGateways.size(); i++)
					{
						AbstractGateway gateway = selectedGateways.get(i);
						Log.getInstance().getLog().debug("Trying message sending via: " + gateway.getGatewayId());
						try
						{
							if (gateway.send(message)) break;
						}
						catch (Exception e)
						{
							Log.getInstance().getLog().error("Unhandled Exception!", e);
						}
					}
					if (message.getSentStatus() != SentStatus.Sent) message.setSentStatus(SentStatus.Failed);
				}
				else
				{
					message.setSentStatus(SentStatus.Failed);
					message.setFailureCause(FailureCause.NoRoute);
				}
			}
			else
			{
				message.setSentStatus(SentStatus.Failed);
				message.setFailureCause(FailureCause.NoRoute);
			}
		}
		else
		{
			message.setSentStatus(SentStatus.Failed);
			message.setFailureCause(FailureCause.NoService);
		}
		return (message.getSentStatus() == SentStatus.Sent);
	}

	public boolean delete(InboundMessage message) throws Exception
	{
		try
		{
			AbstractGateway gateway = getGatewayById(message.getGatewayId());
			return gateway.delete(message);
		}
		catch (Exception e)
		{
			Log.getInstance().getLog().error("Unhandled Exception!", e);
			throw e;
		}
	}

	public DeliveryStatus queryDeliveryStatus(OutboundMessage message) throws Exception
	{
		try
		{
			//TODO: This does not work correctly for multi-id messages!
			if (message.getSentStatus() != SentStatus.Sent) return DeliveryStatus.Unknown;
			return queryDeliveryStatus(getGatewayById(message.getGatewayId()), message.getOperatorMessageIds().get(0));
		}
		catch (Exception e)
		{
			Log.getInstance().getLog().error("Unhandled Exception!", e);
			throw e;
		}
	}

	public DeliveryStatus queryDeliveryStatus(String gatewayId, String operatorMessageId) throws Exception
	{
		try
		{
			return queryDeliveryStatus(getGatewayById(gatewayId), operatorMessageId);
		}
		catch (Exception e)
		{
			Log.getInstance().getLog().error("Unhandled Exception!", e);
			throw e;
		}
	}

	public DeliveryStatus queryDeliveryStatus(AbstractGateway gateway, String operatorMessageId) throws Exception
	{
		try
		{
			return gateway.queryDeliveryStatus(operatorMessageId);
		}
		catch (Exception e)
		{
			Log.getInstance().getLog().error("Unhandled Exception!", e);
			throw e;
		}
	}

	public CreditBalance queryCreditBalance(AbstractGateway gateway) throws Exception
	{
		try
		{
			return gateway.queryCreditBalance();
		}
		catch (Exception e)
		{
			Log.getInstance().getLog().error("Unhandled Exception!", e);
			throw e;
		}
	}

	public Coverage queryCoverage(AbstractGateway gateway, String msisdn) throws Exception
	{
		try
		{
			return gateway.queryCoverage(new Coverage(msisdn));
		}
		catch (Exception e)
		{
			Log.getInstance().getLog().error("Unhandled Exception!", e);
			throw e;
		}
	}

	public boolean registerGateway(AbstractGateway gateway)
	{
		synchronized (this._LOCK_)
		{
			Log.getInstance().getLog().info("Registering Gateway: " + gateway.toShortString());
			getGateways().put(gateway.getGatewayId(), gateway);
			if (getStatus() == Status.Started)
			{
				Log.getInstance().getLog().info("Starting gateway: " + gateway.getGatewayId());
				boolean startStatus = gateway.start();
				if (!startStatus)
				{
					Log.getInstance().getLog().warn(String.format("Gateway %s did not start!", gateway.getGatewayId()));
					getGateways().remove(gateway.getGatewayId());
				}
				return startStatus;
			}
			return true;
		}
	}

	public boolean unregisterGateway(AbstractGateway gateway)
	{
		synchronized (this._LOCK_)
		{
			Log.getInstance().getLog().info("Unregistering Gateway: " + gateway.toShortString());
			getGateways().remove(gateway.getGatewayId());
			if ((getStatus() == Status.Started) || (gateway.getStatus() == AbstractGateway.Status.Started))
			{
				Log.getInstance().getLog().info("Stopping gateway: " + gateway.getGatewayId());
				boolean startStatus = gateway.stop();
				if (!startStatus) Log.getInstance().getLog().warn(String.format("Gateway %s did not stop!", gateway.getGatewayId()));
				return startStatus;
			}
			return true;
		}
	}

	public AbstractGateway getGatewayById(String id)
	{
		return getGateways().get(id);
	}

	public CallbackManager getCallbackManager()
	{
		return this.callbackManager;
	}

	public GroupManager getGroupManager()
	{
		return this.groupManager;
	}

	public LinkedList<OutboundMessage> distributeToGroup(OutboundMessage message)
	{
		LinkedList<OutboundMessage> messageList = new LinkedList<OutboundMessage>();
		if (getGroupManager().exist(message.getRecipient().getNumber()))
		{
			for (MsIsdn msisdn : getGroupManager().getGroup(message.getRecipient().getNumber()).getRecipients())
			{
				OutboundMessage m = new OutboundMessage(message);
				m.setRecipient(new MsIsdn(msisdn));
				messageList.add(m);
			}
		}
		else messageList.add(message);
		return messageList;
	}

	public Collection<AbstractGateway> routeMessage(OutboundMessage message)
	{
		try
		{
			Collection<AbstractGateway> selectedGateways = getRouter().route(message, getGateways().values());
			selectedGateways = getBalancer().balance(message, selectedGateways);
			if (getRouteHook() != null) selectedGateways = getRouteHook().process(message, selectedGateways);
			return selectedGateways;
		}
		catch (ConcurrentModificationException e)
		{
			Log.getInstance().getLog().warn("Gateway list modified, retrying routing...", e);
			return routeMessage(message);
		}
	}

	public AbstractRouter getRouter()
	{
		return this.router;
	}

	public void setRouter(AbstractRouter router)
	{
		this.router = router;
	}

	public AbstractBalancer getBalancer()
	{
		return this.balancer;
	}

	public void setBalancer(AbstractBalancer balancer)
	{
		this.balancer = balancer;
	}

	public Status getStatus()
	{
		return this.status;
	}

	public Statistics getStatistics()
	{
		return this.statistics;
	}

	private void setStatus(Status status)
	{
		Status oldStatus = this.status;
		this.status = status;
		Status newStatus = this.status;
		getCallbackManager().registerServiceStatusEvent(oldStatus, newStatus);
	}

	public HashMap<String, IHttpRequestHandler> getHttpRequestHandlers()
	{
		return this.httpRequestHandlers;
	}

	private HashMap<String, AbstractGateway> getGateways()
	{
		return this.gateways;
	}

	public Collection<String> getGatewayIDs()
	{
		ArrayList<String> listOfGatewayIds = new ArrayList<String>(this.gateways.keySet());
		Collections.sort(listOfGatewayIds);
		return listOfGatewayIds;
	}

	public KeyManager getKeyManager()
	{
		return keyManager;
	}

	public static void main(String[] args)
	{
		System.out.println();
		System.out.println(Settings.LIBRARY_INFO);
		System.out.println(Settings.LIBRARY_COPYRIGHT);
		System.out.println(Settings.LIBRARY_LICENSE);
		System.out.println("SMSLib Version: " + Settings.LIBRARY_VERSION);
		System.out.println("OS Version: " + System.getProperty("os.name") + " / " + System.getProperty("os.arch") + " / " + System.getProperty("os.version"));
		System.out.println("JAVA Version: " + System.getProperty("java.version"));
		System.out.println("JAVA Runtime Version: " + System.getProperty("java.runtime.version"));
		System.out.println("JAVA Vendor: " + System.getProperty("java.vm.vendor"));
		System.out.println("JAVA Class Path: " + System.getProperty("java.class.path"));
		try
		{
			getInstance().terminate();
		}
		catch (Exception e)
		{
			Log.getInstance().getLog().error("Unhandled exception!", e);
			e.printStackTrace();
		}
	}
}
