
package org.smslib.smsserver;

public class SMSServer
{
	public static void main(String[] args)
	{
		System.out.println();
		System.out.println("SMSServer Application - a database driven application based on SMSLib.");
		System.out.println("Version: " + org.smslib.Service.LIBRARY_VERSION);
		System.out.println(org.smslib.Service.LIBRARY_INFO);
		System.out.println(org.smslib.Service.LIBRARY_COPYRIGHT);
		System.out.println(org.smslib.Service.LIBRARY_LICENSE);
		System.out.println("OS Version: " + System.getProperty("os.name") + " / " + System.getProperty("os.arch") + " / " + System.getProperty("os.version"));
		System.out.println("JAVA Version: " + System.getProperty("java.version"));
		System.out.println("JAVA Runtime Version: " + System.getProperty("java.runtime.version"));
		System.out.println("JAVA Vendor: " + System.getProperty("java.vm.vendor"));
		System.out.println("JAVA Class Path: " + System.getProperty("java.class.path"));
		System.out.println();
	}
}
