
package org.smslib.http;

import java.net.InetAddress;
import java.util.List;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.helper.Common;

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
			String ip = request.getClientAddress().getAddress().toString().substring(1);
			String path = request.getPath().toString();
			logger.debug(String.format("IP: %s, PATH: %s", ip, path));
			IHttpRequestHandler h = this.httpServer.getHttpRequestHandlers().get(path);
			if (h != null)
			{
				boolean accessOk = false;
				InetAddress addrIp = InetAddress.getByName(ip);
				List<String> acl = this.httpServer.getHttpRequestACLs().get(path);
				if (acl != null && acl.size() != 0)
				{
					for (String cidr : acl)
					{
						if (Common.checkIPInCIDR(addrIp, cidr))
						{
							accessOk = true;
							break;
						}
					}
				}
				if (accessOk)
				{
					Status s = h.process(request, response);
					response.setStatus(s);
				}
				else response.setStatus(Status.FORBIDDEN);
			}
			else response.setStatus(Status.NOT_FOUND);
			response.close();
		}
		catch (Exception e)
		{
			logger.error("Error!", e);
		}
	}
}
