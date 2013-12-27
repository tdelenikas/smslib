// SMSLib for Java v4
// A universal API for sms messaging.
//
// Copyright (C) 2002-2013, smslib.org
// For more information, visit http://smslib.org
// SMSLib is distributed under the terms of the Apache License version 2.0
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package org.smslib.smsserver;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.Service;
import org.smslib.core.Settings;
import org.smslib.gateway.AbstractGateway;
import org.smslib.groups.Group;
import org.smslib.helper.Common;
import org.smslib.message.MsIsdn;
import org.smslib.routing.NumberRouter;
import org.smslib.smsserver.callback.DeliveryReportCallback;
import org.smslib.smsserver.callback.DequeueMessageCallback;
import org.smslib.smsserver.callback.GatewayStatusCallback;
import org.smslib.smsserver.callback.InboundCallCallback;
import org.smslib.smsserver.callback.InboundMessageCallback;
import org.smslib.smsserver.callback.MessageSentCallback;
import org.smslib.smsserver.callback.ServiceStatusCallback;
import org.smslib.smsserver.hook.PreQueueHook;

public class SMSServer
{
	static Logger logger = LoggerFactory.getLogger(SMSServer.class);

	private static final SMSServer smsserver = new SMSServer();

	public String dbUrl = "";

	public String dbDriver = "";

	public String dbUsername = "";

	public String dbPassword = "";

	public String profile = "";

	public Object LOCK = new Object();

	OutboundServiceThread outboundService;

	public static SMSServer getInstance()
	{
		return smsserver;
	}

	public SMSServer()
	{
	}

	public OutboundServiceThread getOutboundServiceThread()
	{
		return this.outboundService;
	}

	public void setDatabase(String driver, String url, String username, String password)
	{
		this.dbDriver = driver;
		this.dbUrl = url;
		this.dbUsername = username;
		this.dbPassword = password;
	}

	public void startup() throws SQLException, ClassNotFoundException, InterruptedException
	{
		Runtime.getRuntime().addShutdownHook(new ShutdownThread());
		Service.getInstance().setServiceStatusCallback(new ServiceStatusCallback());
		Service.getInstance().setGatewayStatusCallback(new GatewayStatusCallback());
		Service.getInstance().setMessageSentCallback(new MessageSentCallback());
		Service.getInstance().setDequeueMessageCallback(new DequeueMessageCallback());
		Service.getInstance().setPreQueueHook(new PreQueueHook());
		Service.getInstance().setInboundMessageCallback(new InboundMessageCallback());
		Service.getInstance().setDeliveryReportCallback(new DeliveryReportCallback());
		Service.getInstance().setInboundCallCallback(new InboundCallCallback());
		Service.getInstance().start();
		loadGatewayDefinitions();
		loadGroups();
		loadNumberRoutes();
		this.outboundService = new OutboundServiceThread();
	}

	public void shutdown()
	{
		new ShutdownThread().start();
	}

	public Connection getDbConnection() throws ClassNotFoundException, InterruptedException
	{
		Connection db = null;
		while (db == null)
		{
			try
			{
				Class.forName(this.dbDriver);
				db = DriverManager.getConnection(this.dbUrl, this.dbUsername, this.dbPassword);
				db.setAutoCommit(false);
			}
			catch (SQLException e)
			{
				logger.warn("DB error, retrying...", e);
				Thread.sleep(5000);
			}
		}
		return db;
	}

	private void loadGatewayDefinitions() throws ClassNotFoundException, SQLException, InterruptedException
	{
		Connection db = getDbConnection();
		Statement s = db.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = s.executeQuery("select class, gateway_id, p0, p1, p2, p3, p4, p5, sender_address, priority, max_message_parts, delivery_reports from smslib_gateways where (profile = '*' or profile = '" + SMSServer.getInstance().profile + "') and is_enabled = 1");
		while (rs.next())
		{
			int fIndex = 0;
			String className = rs.getString(++fIndex);
			String gatewayId = rs.getString(++fIndex);
			String p0 = rs.getString(++fIndex);
			String p1 = rs.getString(++fIndex);
			String p2 = rs.getString(++fIndex);
			String p3 = rs.getString(++fIndex);
			String p4 = rs.getString(++fIndex);
			String p5 = rs.getString(++fIndex);
			String senderId = rs.getString(++fIndex);
			int priority = rs.getInt(++fIndex);
			int maxMessageParts = rs.getInt(++fIndex);
			boolean requestDeliveryReport = (rs.getInt(++fIndex) == 1);
			logger.info("Registering gateway: " + gatewayId);
			try
			{
				String[] parms = new String[6];
				parms[0] = p0;
				parms[1] = p1;
				parms[2] = p2;
				parms[3] = p3;
				parms[4] = p4;
				parms[5] = p5;
				Object[] args = new Object[] { gatewayId, parms };
				Class<?>[] argsClass = new Class[] { String.class, String[].class };
				Class<?> c = Class.forName(className);
				Constructor<?> constructor = c.getConstructor(argsClass);
				AbstractGateway g = (AbstractGateway) constructor.newInstance(args);
				if (!Common.isNullOrEmpty(senderId)) g.setSenderAddress(new MsIsdn(senderId));
				g.setPriority(priority);
				g.setMaxMessageParts(maxMessageParts);
				g.setRequestDeliveryReport(requestDeliveryReport);
				Service.getInstance().registerGateway(g);
			}
			catch (Exception e)
			{
				logger.error("Gateway " + gatewayId + " did not start properly!", e);
			}
		}
		rs.close();
		s.close();
		db.close();
	}

	private void loadGroups() throws ClassNotFoundException, SQLException, InterruptedException
	{
		Connection db = getDbConnection();
		Statement s1 = db.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs1 = s1.executeQuery("select id, group_name, group_description from smslib_groups where (profile = '*' or profile = '" + SMSServer.getInstance().profile + "')");
		while (rs1.next())
		{
			int groupId = rs1.getInt(1);
			String groupName = rs1.getString(2);
			String groupDescription = rs1.getString(3);
			Group group = new Group(groupName, groupDescription);
			PreparedStatement s2 = db.prepareStatement("select address from smslib_group_recipients where group_id = ?");
			s2.setInt(1, groupId);
			ResultSet rs2 = s2.executeQuery();
			while (rs2.next())
				group.addAddress(new MsIsdn(rs2.getString(1)));
			rs2.close();
			Service.getInstance().getGroupManager().addGroup(group);
		}
		rs1.close();
		s1.close();
		db.close();
	}

	private void loadNumberRoutes() throws ClassNotFoundException, InterruptedException, SQLException
	{
		NumberRouter nr = null;
		Connection db = getDbConnection();
		Statement s = db.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = s.executeQuery("select address_regex, gateway_id from smslib_number_routes where (profile = '*' or profile = '" + SMSServer.getInstance().profile + "') and is_enabled = 1");
		while (rs.next())
		{
			if (nr == null) nr = new NumberRouter();
			String address_regex = rs.getString(1);
			String gatewayId = rs.getString(2);
			nr.addRule(address_regex, Service.getInstance().getGatewayById(gatewayId));
		}
		rs.close();
		s.close();
		db.close();
		if (nr != null) Service.getInstance().setRouter(nr);
	}

	public static void main(String[] args)
	{
		logger.info("SMSServer Application - a database driver application based on SMSLib.");
		logger.info("SMSLib Version: " + Settings.LIBRARY_VERSION);
		logger.info(Settings.LIBRARY_INFO);
		logger.info(Settings.LIBRARY_COPYRIGHT);
		logger.info(Settings.LIBRARY_LICENSE);
		logger.info("For more information, visit http://smslib.org");
		logger.info("OS Version: " + System.getProperty("os.name") + " / " + System.getProperty("os.arch") + " / " + System.getProperty("os.version"));
		logger.info("JAVA Version: " + System.getProperty("java.version"));
		logger.info("JAVA Runtime Version: " + System.getProperty("java.runtime.version"));
		logger.info("JAVA Vendor: " + System.getProperty("java.vm.vendor"));
		logger.info("JAVA Class Path: " + System.getProperty("java.class.path"));
		logger.info("");
		try
		{
			try
			{
				int i = 0;
				while (i < args.length)
				{
					if (args[i].equalsIgnoreCase("-url")) SMSServer.getInstance().dbUrl = args[++i];
					else if (args[i].equalsIgnoreCase("-driver")) SMSServer.getInstance().dbDriver = args[++i];
					else if (args[i].equalsIgnoreCase("-username")) SMSServer.getInstance().dbUsername = args[++i];
					else if (args[i].equalsIgnoreCase("-password")) SMSServer.getInstance().dbPassword = args[++i];
					else if (args[i].equalsIgnoreCase("-profile")) SMSServer.getInstance().profile = args[++i];
					i++;
				}
				if (SMSServer.getInstance().dbUrl.length() == 0 || SMSServer.getInstance().dbDriver.length() == 0 || SMSServer.getInstance().dbUsername.length() == 0) throw new IllegalArgumentException();
				SMSServer.getInstance().startup();
			}
			catch (IllegalArgumentException e)
			{
				logger.info("Illegal / incorrect arguments!");
				logger.info("Parameters: -url 'db-url' -driver 'db-driver' -username 'db-username' -password 'db-password' [-profile 'profile']");
			}
		}
		catch (Exception e)
		{
			logger.error("Unhandled exception!", e);
			System.exit(1);
		}
	}
}
