
package org.smslib.gateway.http.bulksms;

public class BulkSmsDE extends BulkSmsInternational
{
	public BulkSmsDE(String gatewayId, String... parms)
	{
		super(gatewayId, parms[0], parms[1]);
		this.operatorId = "bulksms-de";
		setDescription("BULKSMS DE (http://bulksms.de/)");
		setBaseUrl("http://bulksms.de:5567");
	}
}
