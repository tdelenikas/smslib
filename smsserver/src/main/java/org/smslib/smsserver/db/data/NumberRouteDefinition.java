
package org.smslib.smsserver.db.data;

public class NumberRouteDefinition
{
	String addressRegex;

	String gatewayId;

	public NumberRouteDefinition(String addressRegex, String gatewayId)
	{
		this.addressRegex = addressRegex;
		this.gatewayId = gatewayId;
	}

	public String getAddressRegex()
	{
		return addressRegex;
	}

	public String getGatewayId()
	{
		return gatewayId;
	}
}
