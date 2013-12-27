
package org.smslib.smsserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.Service;
import org.smslib.helper.Common;
import org.smslib.message.AbstractMessage.Encoding;
import org.smslib.message.MsIsdn;
import org.smslib.message.OutboundBinaryMessage;
import org.smslib.message.OutboundMessage;

public class OutboundServiceThread extends Thread
{
	static Logger logger = LoggerFactory.getLogger(OutboundServiceThread.class);

	boolean shouldCancel = false;

	Boolean waitingState = false;

	public OutboundServiceThread()
	{
		setName("Outbound Service Thread");
		start();
	}

	public void shutdown()
	{
		logger.debug("Shutting down...");
		this.shouldCancel = true;
		synchronized (this.waitingState)
		{
			if (this.waitingState) interrupt();
		}
	}

	@Override
	public void run()
	{
		logger.debug("Started!");
		while (!this.shouldCancel)
		{
			try
			{
				queueMessages();
				if (!this.shouldCancel)
				{
					try
					{
						this.waitingState = true;
						Thread.sleep(2000);
						this.waitingState = false;
					}
					catch (InterruptedException e)
					{
						// Shallow this...
					}
				}
			}
			catch (Exception e)
			{
				logger.error("Error!", e);
			}
		}
	}

	private void queueMessages() throws SQLException
	{
		Connection db = null;
		try
		{
			db = SMSServer.getInstance().getDbConnection();
			PreparedStatement s = db.prepareStatement("select message_id, sender_address, address, text, encoding, priority, request_delivery_report, flash_sms from smslib_out where sent_status = ? order by priority desc limit 50");
			s.setString(1, OutboundMessage.SentStatus.Unsent.toShortString());
			ResultSet rs = s.executeQuery();
			while (rs.next())
			{
				String messageId = rs.getString(1);
				if (!Common.isNullOrEmpty(messageId))
				{
					OutboundMessage message;
					String senderId = rs.getString(2);
					String recipient = rs.getString(3);
					String text = rs.getString(4);
					String encoding = rs.getString(5);
					if (Encoding.getEncodingFromShortString(encoding) == Encoding.Enc7)
					{
						message = new OutboundMessage(new MsIsdn(recipient), text);
					}
					else if (Encoding.getEncodingFromShortString(encoding) == Encoding.Enc8)
					{
						message = new OutboundBinaryMessage(new MsIsdn(recipient), Common.stringToBytes(text));
					}
					else if (Encoding.getEncodingFromShortString(encoding) == Encoding.EncUcs2)
					{
						message = new OutboundMessage(new MsIsdn(recipient), text);
						message.setEncoding(Encoding.EncUcs2);
					}
					else
					{
						//TODO: ENC-CUSTOM
						message = new OutboundMessage(new MsIsdn(recipient), text);
					}
					message.setEncoding(Encoding.getEncodingFromShortString(encoding));
					message.setId(messageId);
					if (!Common.isNullOrEmpty(senderId)) message.setOriginatorAddress(new MsIsdn(senderId));
					message.setPriority(rs.getInt(6));
					message.setRequestDeliveryReport(rs.getInt(7) == 1);
					message.setFlashSms(rs.getInt(8) == 1);
					if (!isGroupMessage(message)) Service.getInstance().queue(message);
				}
			}
			rs.close();
			s.close();
		}
		catch (Exception e)
		{
			logger.error("Error!", e);
		}
		finally
		{
			if (db != null) db.close();
		}
	}

	private boolean isGroupMessage(OutboundMessage message) throws ClassNotFoundException, SQLException, InterruptedException
	{
		if (Service.getInstance().getGroupManager().exist(message.getRecipientAddress().getAddress()))
		{
			Connection db = SMSServer.getInstance().getDbConnection();
			PreparedStatement s = db.prepareStatement("update smslib_out set sent_status = '~' where message_id = ?");
			s.setString(1, message.getId());
			s.executeUpdate();
			s.close();
			s = db.prepareStatement("select id from smslib_out where message_id = ?");
			s.setString(1, message.getId());
			ResultSet rs = s.executeQuery();
			rs.next();
			int id = rs.getInt(1);
			rs.close();
			s.close();
			s = db.prepareStatement("insert into smslib_out (parent_id, message_id, sender_id, recipient, text, encoding, priority, request_delivery_report, flash_sms, gateway_id) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			int counter = 0;
			for (MsIsdn recipient : Service.getInstance().getGroupManager().getGroup(message.getRecipientAddress().getAddress()).getRecipients())
			{
				counter++;
				String messageId = String.format("%s?%05d", message.getId(), counter);
				logger.debug("$$$ " + messageId);
				s.setInt(1, id);
				s.setString(2, messageId);
				s.setString(3, (message.getOriginatorAddress() != null && message.getOriginatorAddress().getType() != MsIsdn.Type.Void ? message.getOriginatorAddress().getAddress() : ""));
				s.setString(4, recipient.getAddress());
				s.setString(5, message.getPayload().getText());
				s.setString(6, message.getEncoding().toShortString());
				s.setInt(7, message.getPriority());
				s.setInt(8, (message.getRequestDeliveryReport() ? 1 : 0));
				s.setInt(9, (message.isFlashSms() ? 1 : 1));
				s.setString(10, message.getGatewayId());
				s.executeUpdate();
			}
			s.close();
			db.commit();
			db.close();
			return true;
		}
		return false;
	}
}
