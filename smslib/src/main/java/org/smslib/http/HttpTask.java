
package org.smslib.http;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpTask implements Runnable
{
	static Logger logger = LoggerFactory.getLogger(HttpTask.class);

	Response response;

	Request request;

	HttpServer httpServer;

	public HttpTask(Request request, Response response, HttpServer httpServer)
	{
		this.request = request;
		this.response = response;
		this.httpServer = httpServer;
	}

	@Override
	public void run()
	{
		try
		{
			String visitorIp = request.getClientAddress().getAddress().toString().substring(1);
			String path = request.getPath().toString();
			logger.debug(String.format("IP: %s, PATH: %s", visitorIp, path));
			boolean foundHandler = false;
			for (String p : this.httpServer.getHttpRequestHandlers().keySet())
			{
				if (p.equalsIgnoreCase(path))
				{
					foundHandler = true;
					IHttpRequestHandler h = this.httpServer.getHttpRequestHandlers().get(p);
					Status s = h.process(request, response);
					response.setStatus(s);
					response.setCode(s.code);
				}
			}
			if (!foundHandler) response.setStatus(Status.NOT_FOUND);
			response.close();
		}
		catch (Exception e)
		{
			logger.error("Error!", e);
		}
	}
}
