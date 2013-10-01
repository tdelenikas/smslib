
package org.smslib.smsserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.Service;

public class ShutdownThread extends Thread
{
	static Logger logger = LoggerFactory.getLogger(ShutdownThread.class);

	@Override
	public void run()
	{
		logger.info("SMSServer going down, please wait...");
		try
		{
			if (SMSServer.getInstance().getOutboundServiceThread() != null)
			{
				SMSServer.getInstance().getOutboundServiceThread().shutdown();
				SMSServer.getInstance().getOutboundServiceThread().join();
			}
			Service.getInstance().stop();
			Service.getInstance().terminate();
		}
		catch (Exception e)
		{
			logger.error("Error while terminating the SMSLib service!", e);
		}
	}
}
