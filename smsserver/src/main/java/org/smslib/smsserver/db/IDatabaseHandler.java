
package org.smslib.smsserver.db;

import java.util.Collection;
import org.smslib.callback.events.DeliveryReportCallbackEvent;
import org.smslib.callback.events.InboundCallCallbackEvent;
import org.smslib.callback.events.InboundMessageCallbackEvent;
import org.smslib.message.OutboundMessage;
import org.smslib.smsserver.db.data.GatewayDefinition;
import org.smslib.smsserver.db.data.GroupDefinition;
import org.smslib.smsserver.db.data.NumberRouteDefinition;

public interface IDatabaseHandler
{
	public Collection<GatewayDefinition> getGatewayDefinitions(String profile) throws Exception;

	public Collection<NumberRouteDefinition> getNumberRouteDefinitions(String profile) throws Exception;

	public Collection<GroupDefinition> getGroupDefinitions(String profile) throws Exception;

	public void setMessageStatus(OutboundMessage message, OutboundMessage.SentStatus status) throws Exception;

	public void saveInboundCall(InboundCallCallbackEvent event) throws Exception;

	public void saveDeliveryReport(DeliveryReportCallbackEvent event) throws Exception;

	public void saveInboundMessage(InboundMessageCallbackEvent event) throws Exception;
}
