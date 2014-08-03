
package org.smslib.smsserver.db.data;

public class GatewayDefinition
{
	String className;

	String gatewayId;

	String p0, p1, p2, p3, p4, p5;

	String senderId;

	int priority;

	int maxMessageParts;

	boolean requestDeliveryReport;

	public GatewayDefinition(String className, String gatewayId, String p0, String p1, String p2, String p3, String p4, String p5, String senderId, int priority, int maxMessageParts, boolean requestDeliveryReport)
	{
		this.className = className;
		this.gatewayId = gatewayId;
		this.p0 = p0;
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
		this.p4 = p4;
		this.p5 = p5;
		this.senderId = senderId;
		this.priority = priority;
		this.maxMessageParts = maxMessageParts;
		this.requestDeliveryReport = requestDeliveryReport;
	}

	public String getClassName()
	{
		return className;
	}

	public String getGatewayId()
	{
		return gatewayId;
	}

	public String getP0()
	{
		return p0;
	}

	public String getP1()
	{
		return p1;
	}

	public String getP2()
	{
		return p2;
	}

	public String getP3()
	{
		return p3;
	}

	public String getP4()
	{
		return p4;
	}

	public String getP5()
	{
		return p5;
	}

	public String getSenderId()
	{
		return senderId;
	}

	public int getPriority()
	{
		return priority;
	}

	public int getMaxMessageParts()
	{
		return maxMessageParts;
	}

	public boolean getRequestDeliveryReport()
	{
		return requestDeliveryReport;
	}
}
