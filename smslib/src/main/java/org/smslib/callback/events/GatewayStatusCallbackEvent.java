
package org.smslib.callback.events;

import org.smslib.gateway.AbstractGateway;

public class GatewayStatusCallbackEvent extends BaseCallbackEvent
{
	AbstractGateway gateway;

	AbstractGateway.Status oldStatus;

	AbstractGateway.Status newStatus;

	public GatewayStatusCallbackEvent(AbstractGateway gateway, AbstractGateway.Status oldStatus, AbstractGateway.Status newStatus)
	{
		this.gateway = gateway;
		this.oldStatus = oldStatus;
		this.newStatus = newStatus;
	}

	public AbstractGateway getGateway()
	{
		return this.gateway;
	}

	public AbstractGateway.Status getOldStatus()
	{
		return this.oldStatus;
	}

	public AbstractGateway.Status getNewStatus()
	{
		return this.newStatus;
	}
}
