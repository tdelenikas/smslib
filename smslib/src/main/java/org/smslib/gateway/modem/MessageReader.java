
package org.smslib.gateway.modem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import org.ajwcc.pduUtils.gsm3040.Pdu;
import org.ajwcc.pduUtils.gsm3040.PduParser;
import org.ajwcc.pduUtils.gsm3040.PduUtils;
import org.ajwcc.pduUtils.gsm3040.SmsDeliveryPdu;
import org.ajwcc.pduUtils.gsm3040.SmsStatusReportPdu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.Service;
import org.smslib.core.Settings;
import org.smslib.gateway.AbstractGateway.Status;
import org.smslib.gateway.modem.DeviceInformation.Modes;
import org.smslib.helper.Common;
import org.smslib.message.DeliveryReportMessage;
import org.smslib.message.InboundBinaryMessage;
import org.smslib.message.InboundEncryptedMessage;
import org.smslib.message.InboundMessage;
import org.smslib.message.Payload;

public class MessageReader extends Thread
{
	static Logger logger = LoggerFactory.getLogger(MessageReader.class);

	Modem modem;

	boolean shouldCancel = false;

	public MessageReader(Modem modem)
	{
		this.modem = modem;
	}

	public void cancel()
	{
		logger.debug("Cancelling!");
		this.shouldCancel = true;
	}

	@Override
	public void run()
	{
		logger.debug("Started!");
		while (!this.shouldCancel)
		{
			if (this.modem.getStatus() == Status.Started)
			{
				try
				{
					synchronized (this.modem.getModemDriver()._LOCK_)
					{
						for (int i = 0; i < (this.modem.getModemDriver().getMemoryLocations().length() / 2); i++)
						{
							String memLocation = this.modem.getModemDriver().getMemoryLocations().substring((i * 2), (i * 2) + 2);
							String data = this.modem.getModemDriver().atGetMessages(memLocation).getResponseData();
							if (data.length() > 0)
							{
								ArrayList<InboundMessage> messageList = (this.modem.getDeviceInformation().getMode() == Modes.PDU ? parsePDU(data, memLocation) : parseTEXT(data, memLocation));
								for (InboundMessage message : messageList)
									processMessage(message);
							}
						}
					}
				}
				catch (Exception e)
				{
					logger.error("Unhandled exception!", e);
				}
			}
			if (!this.shouldCancel)
			{
				Common.countSheeps(Settings.modemPollingInterval);
			}
		}
		logger.debug("Stopped!");
	}

	private ArrayList<InboundMessage> parsePDU(String data, String memLocation) throws IOException
	{
		ArrayList<InboundMessage> messageList = new ArrayList<>();
		List<List<InboundMessage>> mpMsgList = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new StringReader(data));
		while (true)
		{
			String line = reader.readLine();
			if (line == null) break;
			PduParser parser = new PduParser();
			int i = line.indexOf(':');
			int j = line.indexOf(',');
			int memIndex = Integer.parseInt(line.substring(i + 1, j).trim());
			i = line.lastIndexOf(',');
			j = line.length();
			int pduSize = Integer.parseInt(line.substring(i + 1, j).trim());
			String pduString = reader.readLine().trim();
			if ((pduSize > 0) && ((pduSize * 2) == pduString.length())) pduString = "00" + pduString;
			Pdu pdu = parser.parsePdu(pduString);
			if (pdu instanceof SmsDeliveryPdu)
			{
				logger.debug("PDU = " + pdu.toString());
				InboundMessage msg = null;
				if (pdu.isBinary())
				{
					msg = new InboundBinaryMessage((SmsDeliveryPdu) pdu, memLocation, memIndex);
					if (Service.getInstance().getKeyManager().getKey(msg.getOriginatorAddress()) != null) msg = new InboundEncryptedMessage((SmsDeliveryPdu) pdu, memLocation, memIndex);
				}
				else
				{
					msg = new InboundMessage((SmsDeliveryPdu) pdu, memLocation, memIndex);
				}
				msg.setGatewayId(this.modem.getGatewayId());
				msg.setGatewayId(this.modem.getGatewayId());
				logger.debug("IN-DTLS: MI:" + msg.getMemIndex() + " REF:" + msg.getMpRefNo() + " MAX:" + msg.getMpMaxNo() + " SEQ:" + msg.getMpSeqNo());
				if (msg.getMpRefNo() == 0) messageList.add(msg);
				else
				{
					// multi-part message
					int k, l;
					List<InboundMessage> tmpList;
					InboundMessage listMsg;
					boolean found, duplicate;
					found = false;
					for (k = 0; k < mpMsgList.size(); k++)
					{
						// List of List<InboundMessage>
						tmpList = mpMsgList.get(k);
						listMsg = tmpList.get(0);
						// check if current message list is for this message
						if (listMsg.getMpRefNo() == msg.getMpRefNo())
						{
							duplicate = false;
							// check if the message is already in the message list
							for (l = 0; l < tmpList.size(); l++)
							{
								listMsg = tmpList.get(l);
								if (listMsg.getMpSeqNo() == msg.getMpSeqNo())
								{
									duplicate = true;
									break;
								}
							}
							if (!duplicate) tmpList.add(msg);
							found = true;
							break;
						}
					}
					if (!found)
					{
						// no existing list present for this message
						// add one
						tmpList = new ArrayList<>();
						tmpList.add(msg);
						mpMsgList.add(tmpList);
					}
				}
			}
			else if (pdu instanceof SmsStatusReportPdu)
			{
				DeliveryReportMessage msg;
				msg = new DeliveryReportMessage((SmsStatusReportPdu) pdu, memLocation, memIndex);
				msg.setGatewayId(this.modem.getGatewayId());
				messageList.add(msg);
			}
		}
		checkMpMsgList(messageList, mpMsgList);
		List<InboundMessage> tmpList;
		for (int k = 0; k < mpMsgList.size(); k++)
		{
			tmpList = mpMsgList.get(k);
			tmpList.clear();
		}
		mpMsgList.clear();
		return messageList;
	}

	private ArrayList<InboundMessage> parseTEXT(String data, String memLocation) throws IOException
	{
		ArrayList<InboundMessage> messageList = new ArrayList<>();
		BufferedReader reader;
		String line;
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		data = data.replaceAll("\\s+OK\\s+", "\nOK");
		data = data.replaceAll("$", "\n");
		logger.debug(data);
		reader = new BufferedReader(new StringReader(data));
		for (;;)
		{
			line = reader.readLine();
			if (line == null) break;
			line = line.trim();
			if (line.length() > 0) break;
		}
		while (true)
		{
			if (line == null) break;
			if (line.length() <= 0 || line.equalsIgnoreCase("OK")) break;
			int i = line.indexOf(':');
			int j = line.indexOf(',');
			int memIndex = Integer.parseInt(line.substring(i + 1, j).trim());
			StringTokenizer tokens = new StringTokenizer(line, ",");
			tokens.nextToken();
			tokens.nextToken();
			String tmpLine = "";
			if (Character.isDigit(tokens.nextToken().trim().charAt(0)))
			{
				line = line.replaceAll(",,", ", ,");
				tokens = new StringTokenizer(line, ",");
				tokens.nextToken();
				tokens.nextToken();
				tokens.nextToken();
				String messageId = tokens.nextToken();
				String recipient = tokens.nextToken().replaceAll("\"", "");
				String dateStr = tokens.nextToken().replaceAll("\"", "");
				if (dateStr.indexOf('/') == -1) dateStr = tokens.nextToken().replaceAll("\"", "");
				cal1.set(Calendar.YEAR, 2000 + Integer.parseInt(dateStr.substring(0, 2)));
				cal1.set(Calendar.MONTH, Integer.parseInt(dateStr.substring(3, 5)) - 1);
				cal1.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateStr.substring(6, 8)));
				dateStr = tokens.nextToken().replaceAll("\"", "");
				cal1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(dateStr.substring(0, 2)));
				cal1.set(Calendar.MINUTE, Integer.parseInt(dateStr.substring(3, 5)));
				cal1.set(Calendar.SECOND, Integer.parseInt(dateStr.substring(6, 8)));
				dateStr = tokens.nextToken().replaceAll("\"", "");
				cal2.set(Calendar.YEAR, 2000 + Integer.parseInt(dateStr.substring(0, 2)));
				cal2.set(Calendar.MONTH, Integer.parseInt(dateStr.substring(3, 5)) - 1);
				cal2.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateStr.substring(6, 8)));
				dateStr = tokens.nextToken().replaceAll("\"", "");
				cal2.set(Calendar.HOUR_OF_DAY, Integer.parseInt(dateStr.substring(0, 2)));
				cal2.set(Calendar.MINUTE, Integer.parseInt(dateStr.substring(3, 5)));
				cal2.set(Calendar.SECOND, Integer.parseInt(dateStr.substring(6, 8)));
				DeliveryReportMessage msg;
				msg = new DeliveryReportMessage(messageId, recipient, memLocation, memIndex, cal1.getTime(), cal2.getTime());
				msg.setGatewayId(this.modem.getGatewayId());
				messageList.add(msg);
			}
			else
			{
				line = line.replaceAll(",,", ", ,");
				tokens = new StringTokenizer(line, ",");
				tokens.nextToken();
				tokens.nextToken();
				String originator = tokens.nextToken().replaceAll("\"", "");
				tokens.nextToken();
				String dateStr = tokens.nextToken().replaceAll("\"", "");
				cal1.set(Calendar.YEAR, 2000 + Integer.parseInt(dateStr.substring(0, 2)));
				cal1.set(Calendar.MONTH, Integer.parseInt(dateStr.substring(3, 5)) - 1);
				cal1.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateStr.substring(6, 8)));
				dateStr = tokens.nextToken().replaceAll("\"", "");
				cal1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(dateStr.substring(0, 2)));
				cal1.set(Calendar.MINUTE, Integer.parseInt(dateStr.substring(3, 5)));
				cal1.set(Calendar.SECOND, Integer.parseInt(dateStr.substring(6, 8)));
				String msgText = "";
				while (true)
				{
					tmpLine = reader.readLine();
					if (tmpLine == null) break;
					if (tmpLine.startsWith("+CMGL")) break;
					if (tmpLine.startsWith("+CMGR")) break;
					msgText += (msgText.length() == 0 ? "" : "\n") + tmpLine;
				}
				InboundMessage msg = new InboundMessage(originator, msgText.trim(), cal1.getTime(), memLocation, memIndex);
				msg.setGatewayId(this.modem.getGatewayId());
				messageList.add(msg);
			}
			while (true)
			{
				//line = reader.readLine();
				line = ((tmpLine == null || tmpLine.length() == 0) ? reader.readLine() : tmpLine);
				if (line == null) break;
				line = line.trim();
				if (line.length() > 0) break;
			}
		}
		reader.close();
		return messageList;
	}

	private void checkMpMsgList(Collection<InboundMessage> msgList, List<List<InboundMessage>> mpMsgList)
	{
		int k, l, m;
		List<InboundMessage> tmpList;
		InboundMessage listMsg, mpMsg;
		boolean found;
		mpMsg = null;
		logger.debug("CheckMpMsgList(): MAINLIST: " + mpMsgList.size());
		for (k = 0; k < mpMsgList.size(); k++)
		{
			tmpList = mpMsgList.get(k);
			logger.debug("CheckMpMsgList(): SUBLIST[" + k + "]: " + tmpList.size());
			listMsg = tmpList.get(0);
			found = false;
			if (listMsg.getMpMaxNo() == tmpList.size())
			{
				found = true;
				for (l = 0; l < tmpList.size(); l++)
					for (m = 0; m < tmpList.size(); m++)
					{
						listMsg = tmpList.get(m);
						if (listMsg.getMpSeqNo() == (l + 1))
						{
							if (listMsg.getMpSeqNo() == 1)
							{
								mpMsg = listMsg;
								mpMsg.setMpMemIndex(mpMsg.getMemIndex());
								if (listMsg.getMpMaxNo() == 1) msgList.add(mpMsg);
							}
							else
							{
								if (mpMsg != null)
								{
									//TODO:
									//if (mpMsg instanceof InboundBinaryMessage)
									if (false)
									{
										//InboundBinaryMessage mpMsgBinary = (InboundBinaryMessage) mpMsg;
										//InboundBinaryMessage listMsgBinary = (InboundBinaryMessage) listMsg;
										//mpMsgBinary.addDataBytes(listMsgBinary.getDataBytes());
									}
									else
									{
										// NEW
										String textToAdd = listMsg.getPayload().getText();
										if (mpMsg.getEndsWithMultiChar())
										{
											// adjust first char of textToAdd
											logger.debug("Adjusting dangling multi-char: " + textToAdd.charAt(0) + " --> " + PduUtils.getMultiCharFor(textToAdd.charAt(0)));
											textToAdd = PduUtils.getMultiCharFor(textToAdd.charAt(0)) + textToAdd.substring(1);
										}
										mpMsg.setEndsWithMultiChar(listMsg.getEndsWithMultiChar());
										mpMsg.setPayload(new Payload(mpMsg.getPayload().getText() + textToAdd));
									}
									mpMsg.setMpSeqNo(listMsg.getMpSeqNo());
									mpMsg.setMpMemIndex(listMsg.getMemIndex());
									if (listMsg.getMpSeqNo() == listMsg.getMpMaxNo())
									{
										mpMsg.setMemIndex(-1);
										msgList.add(mpMsg);
										mpMsg = null;
									}
								}
							}
							break;
						}
					}
				tmpList.clear();
				tmpList = null;
			}
			if (found)
			{
				mpMsgList.remove(k);
				k--;
			}
		}
		// Check the remaining parts for "orphaned" status
		for (List<InboundMessage> remainingList : mpMsgList)
		{
			for (InboundMessage msg : remainingList)
			{
				if (Common.getAgeInHours(msg.getSentDate()) > Settings.hoursToRetainOrphanedMessageParts)
				{
					try
					{
						this.modem.delete(msg);
					}
					catch (Exception e)
					{
						logger.error("Could not delete orphaned message: " + msg.toString(), e);
					}
					//deleteMessage(msg);
					//
					//TODO: Add Orphaned notifications!
					// if (Service.getInstance().getOrphanedMessageNotification() != null) if (Service.getInstance().getOrphanedMessageNotification().process(Service.getInstance().getGateway(msg.getGatewayId()), msg) == true) deleteMessage(msg);
					//
				}
			}
		}
	}

	private void processMessage(InboundMessage message)
	{
		String messageSignature = message.getSignature();
		if (!this.modem.getReadMessagesSet().contains(messageSignature))
		{
			this.modem.getStatistics().increaseTotalReceived();
			if (message instanceof DeliveryReportMessage) Service.getInstance().getCallbackManager().registerDeliveryReportEvent((DeliveryReportMessage) message);
			else Service.getInstance().getCallbackManager().registerInboundMessageEvent(message);
			this.modem.getReadMessagesSet().add(messageSignature);
		}
	}
}
