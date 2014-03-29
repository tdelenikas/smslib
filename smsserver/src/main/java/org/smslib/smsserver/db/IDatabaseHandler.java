
package org.smslib.smsserver.db;

import java.util.Collection;
import org.smslib.smsserver.db.data.GatewayDefinition;
import org.smslib.smsserver.db.data.NumberRouteDefinition;

public interface IDatabaseHandler
{
	public Collection<GatewayDefinition> getGatewayDefinitions(String profile) throws Exception;
	public Collection<NumberRouteDefinition> getNumberRouteDefinitions(String profile) throws Exception;
}
