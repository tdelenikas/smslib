
package org.smslib.gateway.http.bulksms;

public class BulkSmsUK extends BulkSmsInternational
{
	public BulkSmsUK(String gatewayId, String... parms)
	{
		super(gatewayId, parms[0], parms[1]);
		this.operatorId = "bulksms-uk";
		setDescription("BULKSMS UK (http://www.bulksms.co.uk/)");
		setBaseUrl("http://www.bulksms.co.uk:5567");
	}
}
