package org.smslib.smsserver.db.data;

public class GatewayDefinition
{
	public String className;
	public String gatewayId;
	public String p0, p1, p2, p3, p4, p5;
	public String senderId;
	public int priority;
	public int maxMessageParts;
	public boolean requestDeliveryReport;
}
