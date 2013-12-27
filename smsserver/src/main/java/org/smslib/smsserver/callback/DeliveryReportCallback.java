
package org.smslib.smsserver.callback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.callback.IDeliveryReportCallback;
import org.smslib.callback.events.DeliveryReportCallbackEvent;
import org.smslib.smsserver.SMSServer;

public class DeliveryReportCallback implements IDeliveryReportCallback
{
	static Logger logger = LoggerFactory.getLogger(DeliveryReportCallback.class);

	@Override
	public boolean process(DeliveryReportCallbackEvent event)
	{
		Connection db = null;
		PreparedStatement s = null;
		try
		{
			db = SMSServer.getInstance().getDbConnection();
			s = db.prepareStatement("update smslib_out set delivery_status = ?, delivery_date = ? where address = ? and operator_message_id = ? and gateway_id = ?");
			s.setString(1, event.getMessage().getDeliveryStatus().toShortString());
			s.setTimestamp(2, new Timestamp(event.getMessage().getOriginalReceivedDate().getTime()));
			s.setString(3, event.getMessage().getRecipientAddress().getAddress());
			s.setString(4, event.getMessage().getOriginalOperatorMessageId());
			s.setString(5, event.getMessage().getGatewayId());
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
