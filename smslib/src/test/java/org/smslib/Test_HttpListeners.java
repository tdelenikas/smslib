
package org.smslib;

import java.io.PrintStream;
import junit.framework.TestCase;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.http.IHttpRequestHandler;

public class Test_HttpListeners extends TestCase
{
	static Logger logger = LoggerFactory.getLogger(Test_HttpListeners.class);

	public class HttpListener01 implements IHttpRequestHandler
	{
		@Override
		public Status process(Request request, Response response)
		{
			PrintStream output = null;
			try
			{
				output = response.getPrintStream();
				output.println("Hello World!");
				return Status.OK;
			}
			catch (Exception e)
			{
				logger.error("Error!", e);
				return Status.INTERNAL_SERVER_ERROR;
			}
			finally
			{
				if (output != null) output.close();
			}
		}
	}

	public void test() throws Exception
	{
		Service.getInstance().registerHttpRequestHandler("/test-endpoint/test01", new HttpListener01());
		Service.getInstance().registerHttpRequestACL("/test-endpoint/test01", "192.168.0.0/16");
		Service.getInstance().registerHttpRequestACL("/test-endpoint/test01", "127.0.0.1/0");
		Service.getInstance().start();
		Thread.sleep(30000);
		Service.getInstance().stop();
		Service.getInstance().terminate();
	}
}
