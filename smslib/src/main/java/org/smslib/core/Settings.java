
package org.smslib.core;

public class Settings
{
	public static final String LIBRARY_INFO = "SMSLib - A universal API for sms messaging";

	public static final String LIBRARY_LICENSE = "This software is distributed under the terms of the\nApache v2.0 License (http://www.apache.org/licenses/LICENSE-2.0.html).";

	public static final String LIBRARY_COPYRIGHT = "Copyright (c) 2002-2015, smslib.org";

	public static final String LIBRARY_VERSION = "dev-SNAPSHOT";

	public static int httpServerPort = 8001;

	public static int httpServerPoolSize = 10;

	public static int serviceDispatcherQueueTimeout = 1000;

	public static int serviceDispatcherYield = 0;

	public static int gatewayDispatcherQueueTimeout = 1000;

	public static int gatewayDispatcherYield = 0;

	public static int callbackDispatcherQueueTimeout = 1000;

	public static int callbackDispatcherYield = 0;

	public static int daemonDispatcherYield = 10000;

	public static int modemPollingInterval = 15000;

	public static boolean keepOutboundMessagesInQueue = true;

	public static int hoursToRetainOrphanedMessageParts = 72;

	public static boolean deleteMessagesAfterCallback = false;

	public static void loadSettings()
	{
		if (System.getProperty("smslib.httpserver.port") != null) httpServerPort = Integer.parseInt(System.getProperty("smslib.httpserver.port"));
		if (System.getProperty("smslib.httpserver.poolsize") != null) httpServerPoolSize = Integer.parseInt(System.getProperty("smslib.httpserver.poolsize"));
		if (System.getProperty("smslib.keepoutboundmessagesinqueue") != null) keepOutboundMessagesInQueue = Boolean.valueOf(System.getProperty("smslib.keepoutboundmessagesinqueue"));
		if (System.getProperty("smslib.hourstoretainorphanedmessageparts") != null) hoursToRetainOrphanedMessageParts = Integer.valueOf(System.getProperty("smslib.hourstoretainorphanedmessageparts"));
		if (System.getProperty("smslib.deletemessagesaftercallback") != null) deleteMessagesAfterCallback = Boolean.valueOf(System.getProperty("smslib.deletemessagesaftercallback"));
	}
}
