
package org.smslib.callback.events;

import org.smslib.message.MsIsdn;

public class InboundCallEvent extends BaseCallbackEvent
{
	MsIsdn msisdn;

	String gatewayId;

	public InboundCallEvent(MsIsdn msisdn, String gatewayId)
	{
		this.msisdn = msisdn;
		this.gatewayId = gatewayId;
	}

	public MsIsdn getMsisdn()
	{
		return this.msisdn;
	}

	public String getGatewayId()
	{
		return this.gatewayId;
	}
}
