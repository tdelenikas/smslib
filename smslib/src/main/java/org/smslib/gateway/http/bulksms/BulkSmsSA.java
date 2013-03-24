
package org.smslib.gateway.http.bulksms;

public class BulkSmsSA extends BulkSmsInternational
{
	public BulkSmsSA(String gatewayId, String... parms)
	{
		super(gatewayId, parms[0], parms[1]);
		this.operatorId = "bulksms-sa";
		setDescription("BULKSMS SA (http://bulksms.2way.co.za/)");
		setBaseUrl("http://bulksms.2way.co.za:5567");
	}
}
