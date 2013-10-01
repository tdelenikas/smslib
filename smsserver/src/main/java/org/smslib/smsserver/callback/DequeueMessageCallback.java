
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

	Connection db = null;

	@Override
	public boolean process(DequeueMessageCallbackEvent event)
	{
		try
		{
			if (this.db == null) this.db = SMSServer.getInstance().getDbConnection();
			PreparedStatement s = this.db.prepareStatement("update smslib_out set sent_status = 'U' where message_id = ?");
			s.setString(1, event.getMessage().getId());
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
