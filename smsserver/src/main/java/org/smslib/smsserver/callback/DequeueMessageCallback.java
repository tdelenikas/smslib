
package org.smslib.smsserver.callback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.callback.IDequeueMessageCallback;
import org.smslib.callback.events.DequeueMessageCallbackEvent;
import org.smslib.smsserver.SMSServer;

public class DequeueMessageCallback implements IDequeueMessageCallback
{
	static Logger logger = LoggerFactory.getLogger(DequeueMessageCallback.class);

	@Override
	public boolean process(DequeueMessageCallbackEvent event)
	{
		Connection db = null;
		PreparedStatement s = null;
		try
		{
			db = SMSServer.getInstance().getDbConnection();
			s = db.prepareStatement("update smslib_out set sent_status = 'U' where message_id = ?");
			s.setString(1, event.getMessage().getId());
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
	}
}
