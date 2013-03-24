
package org.smslib.gateway.http.bulksms;

public class BulkSmsES extends BulkSmsInternational
{
	public BulkSmsES(String gatewayId, String... parms)
	{
		super(gatewayId, parms[0], parms[1]);
		this.operatorId = "bulksms-es";
		setDescription("BULKSMS ES (http://bulksms.com.es/)");
		setBaseUrl("http://bulksms.com.es:5567");
	}
}
