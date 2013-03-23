
package org.smslib.helper;

import java.io.File;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log
{
	private static final Log logger = new Log();

	Logger log;

	private Log()
	{
		this.log = LoggerFactory.getLogger("smslib");
		if (System.getProperties().getProperty("java.vm.name").equalsIgnoreCase("ikvm.net"))
		{
			File f = new File("log4j.properties");
			if (f.exists()) PropertyConfigurator.configure("log4j.properties");
		}
	}

	public static Log getInstance()
	{
		return Log.logger;
	}

	public Logger getLog()
	{
		return this.log;
	}
}
