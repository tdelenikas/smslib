package org.smslib.smsserver.db.data;

import java.util.Date;
import org.smslib.message.MsIsdn;

public class InboundCallDefinition
{
	Date date;
	MsIsdn msisdn;
	String gatewayId;

	public InboundCallDefinition(Date date, MsIsdn msisdn, String gatewayId)
	{
		this.date = date;
		this.msisdn = msisdn;
		this.gatewayId = gatewayId;
	}

	public Date getDate()
	{
		return date;
	}

	public MsIsdn getMsisdn()
	{
		return msisdn;
	}

	public String getGatewayId()
	{
		return gatewayId;
	}
}
