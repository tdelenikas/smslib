
package org.smslib.helper;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import org.smslib.gateway.AbstractGateway;
import org.smslib.message.OutboundMessage;

public class Common
{
	public static boolean isNullOrEmpty(String s)
	{
		return ((s == null) || (s.trim().length() == 0));
	}

	public static Date getMinDate()
	{
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_YEAR, 1);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.YEAR, 1000);
		return cal.getTime();
	}

	public static String dumpRoutingTable(OutboundMessage message)
	{
		StringBuffer b = new StringBuffer(1024);
		b.append(String.format("Message Id: %s%s", message.getId(), System.getProperty("line.separator")));
		b.append(String.format("Message Recipient: %s%s", message.getRecipientAddress(), System.getProperty("line.separator")));
		b.append("Routed via: ");
		for (AbstractGateway g : message.getRoutingTable())
			b.append(g.getGatewayId() + " -> ");
		b.append(System.getProperty("line.separator"));
		return b.toString();
	}

	public static boolean checkIPInCIDR(InetAddress ip, String CIDR) throws UnknownHostException
	{
		StringTokenizer tokens = new StringTokenizer(CIDR, "/");
		int cidrIP = InetAddressToInt(tokens.nextToken());
		int cidrMask = Integer.parseInt(tokens.nextToken());
		cidrMask = (-1) << (32 - cidrMask);
		int lowest = cidrIP & cidrMask;
		int highest = lowest + (~cidrMask);
		int targetIp = addressToInt(ip);
		return ((targetIp >= lowest) && (targetIp <= highest));
	}

	public static int addressToInt(InetAddress ip)
	{
		int compacted = 0;
		byte[] bytes = ip.getAddress();
		for (int i = 0; i < bytes.length; i++)
		{
			compacted += (bytes[i] * Math.pow(256, 4 - i - 1));
		}
		return compacted;
	}

	public static int InetAddressToInt(String ip) throws UnknownHostException
	{
		return addressToInt(InetAddress.getByName(ip));
	}

	public static void countSheeps(int n)
	{
		try
		{
			Thread.sleep(n);
		}
		catch (InterruptedException e)
		{
			// Nothing here...
		}
	}

	public static int getAgeInHours(Date fromDate)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(new java.util.Date());
		long now = cal.getTimeInMillis();
		cal.setTime(fromDate);
		long past = cal.getTimeInMillis();
		return (int) ((now - past) / (60 * 60 * 1000));
	}

	public static byte[] stringToBytes(String data)
	{
		byte bytes[] = new byte[data.length() / 2];
		for (int i = 0; i < data.length(); i += 2)
		{
			int value = (Integer.parseInt("" + data.charAt(i), 16) * 16) + (Integer.parseInt("" + data.charAt(i + 1), 16));
			bytes[i / 2] = (byte) value;
		}
		return bytes;
	}

	public static String bytesToString(byte[] bytes)
	{
		BigInteger bi = new BigInteger(1, bytes);
		return String.format("%0" + (bytes.length << 1) + "x", bi);
	}
}
