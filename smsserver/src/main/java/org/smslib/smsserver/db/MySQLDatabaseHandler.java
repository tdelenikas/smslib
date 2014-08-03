
package org.smslib.smsserver.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.smsserver.SMSServer;
import org.smslib.smsserver.db.data.DeliveryReportDefinition;
import org.smslib.smsserver.db.data.GatewayDefinition;
import org.smslib.smsserver.db.data.GroupDefinition;
import org.smslib.smsserver.db.data.GroupRecipientDefinition;
import org.smslib.smsserver.db.data.InboundCallDefinition;
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

	@Override
	public Collection<GroupDefinition> getGroupDefinitions(String profile) throws Exception
	{
		List<GroupDefinition> groups = new LinkedList<GroupDefinition>();
		Connection db = getDbConnection();
		Statement s1 = db.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs1 = s1.executeQuery("select id, group_name, group_description from smslib_groups where (profile = '*' or profile = '" + profile + "') and is_enabled = 1");
		while (rs1.next())
		{
			int groupId = rs1.getInt(1);
			String groupName = rs1.getString(2).trim();
			String groupDescription = rs1.getString(3).trim();
			List<GroupRecipientDefinition> recipients = new LinkedList<GroupRecipientDefinition>();
			PreparedStatement s2 = db.prepareStatement("select address from smslib_group_recipients where group_id = ? and is_enabled = 1");
			s2.setInt(1, groupId);
			ResultSet rs2 = s2.executeQuery();
			while (rs2.next())
				recipients.add(new GroupRecipientDefinition(rs2.getString(1).trim()));
			rs2.close();
			groups.add(new GroupDefinition(groupName, groupDescription, recipients));
		}
		rs1.close();
		s1.close();
		db.close();
		return groups;
	}

	@Override
	public void SetMessage(String messageId, String status) throws Exception
	{
		Connection db = null;
		PreparedStatement s = null;
		try
		{
			db = getDbConnection();
			s = db.prepareStatement("update smslib_out set sent_status = ? where message_id = ?");
			s.setString(1, status);
			s.setString(2, messageId);
			s.executeUpdate();
			db.commit();
		}
		catch (Exception e)
		{
			if (db != null) db.rollback();
			logger.error("Error!", e);
			throw e;
		}
		finally
		{
			if (s != null) s.close();
			if (db != null) db.close();
		}
	}

	@Override
	public void SaveInboundCall(InboundCallDefinition inboundCall) throws Exception
	{
		Connection db = null;
		PreparedStatement s = null;
		try
		{
			db = getDbConnection();
			s = db.prepareStatement("insert into smslib_calls (date, address, gateway_id) values (?, ?, ?)");
			s.setTimestamp(1, new Timestamp(inboundCall.getDate().getTime()));
			s.setString(2, inboundCall.getMsisdn().getAddress());
			s.setString(3, inboundCall.getGatewayId());
			s.executeUpdate();
			db.commit();
		}
		catch (Exception e)
		{
			if (db != null) db.rollback();
			logger.error("Error!", e);
			throw e;
		}
		finally
		{
			if (s != null) s.close();
			if (db != null) db.close();
		}
	}

	@Override
	public void SaveDeliveryReport(DeliveryReportDefinition deliveryReport) throws Exception
	{
		Connection db = null;
		PreparedStatement s = null;
		try
		{
			db = getDbConnection();
			s = db.prepareStatement("update smslib_out set delivery_status = ?, delivery_date = ? where address = ? and operator_message_id = ? and gateway_id = ?");
			s.setString(1, deliveryReport.getDeliveryStatus());
			s.setTimestamp(2, new Timestamp(deliveryReport.getOriginalReceivedDate().getTime()));
			s.setString(3, deliveryReport.getRecipientAddress().getAddress());
			s.setString(4, deliveryReport.getOriginalMessageId());
			s.setString(5, deliveryReport.getGatewayId());
			s.executeUpdate();
			db.commit();
		}
		catch (Exception e)
		{
			if (db != null) db.rollback();
			logger.error("Error!", e);
			throw e;
		}
		finally
		{
			if (s != null) s.close();
			if (db != null) db.close();
		}
	}
}
