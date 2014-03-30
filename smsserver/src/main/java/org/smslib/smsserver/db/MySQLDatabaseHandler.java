
package org.smslib.smsserver.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.smsserver.SMSServer;
import org.smslib.smsserver.db.data.GatewayDefinition;
import org.smslib.smsserver.db.data.NumberRouteDefinition;

public class MySQLDatabaseHandler extends JDBCDatabaseHandler implements IDatabaseHandler
{
	static Logger logger = LoggerFactory.getLogger(SMSServer.class);

	public MySQLDatabaseHandler(String dbUrl, String dbDriver, String dbUsername, String dbPassword)
	{
		super(dbUrl, dbDriver, dbUsername, dbPassword);
	}

	@Override
	public Collection<GatewayDefinition> getGatewayDefinitions(String profile) throws Exception
	{
		List<GatewayDefinition> gatewayList = new LinkedList<GatewayDefinition>();
		Connection db = getDbConnection();
		Statement s = db.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = s.executeQuery("select class, gateway_id, ifnull(p0, ''), ifnull(p1, ''), ifnull(p2, ''), ifnull(p3, ''), ifnull(p4, ''), ifnull(p5, ''), sender_address, priority, max_message_parts, delivery_reports from smslib_gateways where (profile = '*' or profile = '" + profile + "') and is_enabled = 1");
		while (rs.next())
		{
			int fIndex = 0;
			GatewayDefinition g = new GatewayDefinition(rs.getString(++fIndex).trim(), rs.getString(++fIndex).trim(), rs.getString(++fIndex).trim(), rs.getString(++fIndex).trim(), rs.getString(++fIndex).trim(), rs.getString(++fIndex).trim(), rs.getString(++fIndex).trim(), rs.getString(++fIndex).trim(), rs.getString(++fIndex).trim(), rs.getInt(++fIndex), rs.getInt(++fIndex), (rs.getInt(++fIndex) == 1));
			gatewayList.add(g);
		}
		rs.close();
		s.close();
		db.close();
		return gatewayList;
	}

	@Override
	public Collection<NumberRouteDefinition> getNumberRouteDefinitions(String profile) throws Exception
	{
		List<NumberRouteDefinition> routeList = new LinkedList<NumberRouteDefinition>();
		Connection db = getDbConnection();
		Statement s = db.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = s.executeQuery("select address_regex, gateway_id from smslib_number_routes where (profile = '*' or profile = '" + profile + "') and is_enabled = 1");
		while (rs.next())
		{
			NumberRouteDefinition r = new NumberRouteDefinition(rs.getString(1).trim(), rs.getString(2).trim());
			routeList.add(r);
		}
		rs.close();
		s.close();
		db.close();
		return routeList;
	}
}
