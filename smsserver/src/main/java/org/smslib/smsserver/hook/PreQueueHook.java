
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

	Connection db = null;

	@Override
	public boolean process(OutboundMessage message)
	{
		try
		{
			if (this.db == null) this.db = SMSServer.getInstance().getDbConnection();
			PreparedStatement s = this.db.prepareStatement("update smslib_out set sent_status = ? where message_id = ?");
			s.setString(1, OutboundMessage.SentStatus.Queued.toShortString());
			s.setString(2, message.getId());
			s.executeUpdate();
			s.close();
			this.db.commit();
			return true;
		}
		catch (Exception e)
		{
			logger.error("Error!", e);
			if (this.db != null)
			{
				try
				{
					this.db.close();
				}
				catch (SQLException e1)
				{
					//Shallow exception on purpose...
				}
			}
			this.db = null;
			return false;
		}
	}
}
