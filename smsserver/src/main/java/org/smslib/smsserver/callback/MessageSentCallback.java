
package org.smslib.smsserver.callback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.callback.IMessageSentCallback;
import org.smslib.callback.events.MessageSentCallbackEvent;
import org.smslib.smsserver.SMSServer;
import org.smslib.message.OutboundMessage.SentStatus;

public class MessageSentCallback implements IMessageSentCallback
{
	static Logger logger = LoggerFactory.getLogger(MessageSentCallback.class);

	Connection db = null;

	@Override
	public boolean process(MessageSentCallbackEvent event)
	{
		try
		{
			if (this.db == null) this.db = SMSServer.getInstance().getDbConnection();
			PreparedStatement s = this.db.prepareStatement("update smslib_out set sent_status = ?, sent_date = ?, gateway_id = ?, operator_message_id = ? where message_id = ?");
			s.setString(1, event.getMessage().getSentStatus().toShortString());
			s.setTimestamp(2, new Timestamp((event.getMessage().getSentStatus() == SentStatus.Sent ? event.getMessage().getSentDate().getTime() : 0)));
			s.setString(3, (event.getMessage().getSentStatus() == SentStatus.Sent ? event.getMessage().getGatewayId() : ""));
			s.setString(4, (event.getMessage().getSentStatus() == SentStatus.Sent ? event.getMessage().getOperatorMessageId() : ""));
			s.setString(5, event.getMessage().getId());
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
