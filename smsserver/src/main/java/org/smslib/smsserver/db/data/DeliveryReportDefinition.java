package org.smslib.smsserver.db.data;

import java.util.Date;
import org.smslib.message.MsIsdn;

public class DeliveryReportDefinition
{
	String deliveryStatus;
	Date originalReceivedDate;
	MsIsdn recipientAddress;
	String originalMessageId;
	String gatewayId;

	public DeliveryReportDefinition(String deliveryStatus, Date originalReceivedDate, MsIsdn recipientAddress, String originalMessageId, String gatewayId)
	{
		this.deliveryStatus = deliveryStatus;
		this.originalReceivedDate = originalReceivedDate;
		this.recipientAddress = recipientAddress;
		this.originalMessageId = originalMessageId;
		this.gatewayId = gatewayId;
	}

	public String getDeliveryStatus()
	{
		return deliveryStatus;
	}

	public Date getOriginalReceivedDate()
	{
		return originalReceivedDate;
	}

	public MsIsdn getRecipientAddress()
	{
		return recipientAddress;
	}

	public String getOriginalMessageId()
	{
		return originalMessageId;
	}

	public String getGatewayId()
	{
		return gatewayId;
	}
}
