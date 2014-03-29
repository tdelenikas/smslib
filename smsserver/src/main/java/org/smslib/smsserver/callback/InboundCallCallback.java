
package org.smslib.smsserver.callback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.callback.IInboundCallCallback;
import org.smslib.callback.events.InboundCallEvent;
import org.smslib.smsserver.SMSServer;

public class InboundCallCallback implements IInboundCallCallback
{
	static Logger logger = LoggerFactory.getLogger(InboundCallCallback.class);

	@Override
	public boolean process(InboundCallEvent event)
	{
/*
		Connection db = null;
		PreparedStatement s = null;
		try
		{
			db = SMSServer.getInstance().getDbConnection();
			s = db.prepareStatement("insert into smslib_calls (date, address, gateway_id) values (?, ?, ?)");
			s.setTimestamp(1, new Timestamp(event.getDate().getTime()));
			s.setString(2, event.getMsisdn().getAddress());
			s.setString(3, event.getGatewayId());
			s.executeUpdate();
			db.commit();
			return true;
		}
		catch (Exception e)
		{
			logger.error("Error!", e);
			return false;
		}
		finally
		{
			if (s != null)
			{
				try
				{
					s.close();
				}
				catch (SQLException e)
				{
					logger.error("Error!", e);
				}
			}
			if (db != null)
			{
				try
				{
					db.close();
				}
				catch (SQLException e)
				{
					logger.error("Error!", e);
				}
			}
		}
*/
		return false;
	}
}
