
package org.smslib.smsserver.hook;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.hook.IPreQueueHook;
import org.smslib.message.OutboundMessage;
import org.smslib.smsserver.SMSServer;

public class PreQueueHook implements IPreQueueHook
{
	static Logger logger = LoggerFactory.getLogger(PreQueueHook.class);

	@Override
	public boolean process(OutboundMessage message)
	{
		Connection db = null;
		try
		{
			db = SMSServer.getInstance().getDbConnection();
			PreparedStatement s = db.prepareStatement("update smslib_out set sent_status = ? where message_id = ?");
			s.setString(1, OutboundMessage.SentStatus.Queued.toShortString());
			s.setString(2, message.getId());
			s.executeUpdate();
			s.close();
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
