
package org.smslib.gateway.http.bulksms;

public class BulkSmsUS extends BulkSmsInternational
{
	public BulkSmsUS(String gatewayId, String... parms)
	{
		super(gatewayId, parms[0], parms[1]);
		this.operatorId = "bulksms-us";
		setDescription("BULKSMS US (http://usa.bulksms.com/)");
		setBaseUrl("http://usa.bulksms.com:5567");
	}
}
