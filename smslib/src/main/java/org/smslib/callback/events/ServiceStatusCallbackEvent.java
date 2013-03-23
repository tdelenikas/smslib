
package org.smslib.callback.events;

import org.smslib.Service;

public class ServiceStatusCallbackEvent extends BaseCallbackEvent
{
	Service.Status oldStatus;

	Service.Status newStatus;

	public ServiceStatusCallbackEvent(Service.Status oldStatus, Service.Status newStatus)
	{
		this.oldStatus = oldStatus;
		this.newStatus = newStatus;
	}

	public Service.Status getOldStatus()
	{
		return this.oldStatus;
	}

	public Service.Status getNewStatus()
	{
		return this.newStatus;
	}
}
