
package org.smslib.smsserver;

import org.smslib.core.Settings;

public class SMSServer
{
	public static void main(String[] args)
	{
		System.out.println();
		System.out.println("SMSServer Application - a database driven application based on SMSLib.");
		System.out.println("Version: " + Settings.LIBRARY_VERSION);
		System.out.println(Settings.LIBRARY_INFO);
		System.out.println(Settings.LIBRARY_COPYRIGHT);
		System.out.println(Settings.LIBRARY_LICENSE);
		System.out.println("OS Version: " + System.getProperty("os.name") + " / " + System.getProperty("os.arch") + " / " + System.getProperty("os.version"));
		System.out.println("JAVA Version: " + System.getProperty("java.version"));
		System.out.println("JAVA Runtime Version: " + System.getProperty("java.runtime.version"));
		System.out.println("JAVA Vendor: " + System.getProperty("java.vm.vendor"));
		System.out.println("JAVA Class Path: " + System.getProperty("java.class.path"));
		System.out.println();
	}
}
