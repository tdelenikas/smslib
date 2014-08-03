
package org.smslib.smsserver.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.smsserver.SMSServer;

public abstract class JDBCDatabaseHandler implements IDatabaseHandler
{
	static Logger logger = LoggerFactory.getLogger(SMSServer.class);

	String dbUrl = "";

	String dbDriver = "";

	String dbUsername = "";

	String dbPassword = "";

	public JDBCDatabaseHandler(String dbUrl, String dbDriver, String dbUsername, String dbPassword)
	{
		this.dbUrl = dbUrl;
		this.dbDriver = dbDriver;
		this.dbUsername = dbUsername;
		this.dbPassword = dbPassword;
	}

	protected Connection getDbConnection() throws Exception
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
}
