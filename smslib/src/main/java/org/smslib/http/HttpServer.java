
package org.smslib.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.http.handlers.StatusHandler;

public class HttpServer implements Container
{
	static Logger logger = LoggerFactory.getLogger(HttpServer.class);

	static HashMap<String, IHttpRequestHandler> httpRequestHandlers = new HashMap<>();

	static HashMap<String, List<String>> httpRequestACLs = new HashMap<>();

	static Executor executor;

	Container container;

	Connection connection;

	Server server;

	public HttpServer()
	{
		HttpServer.httpRequestHandlers.put("/status", new StatusHandler());
	}

	public void start(InetSocketAddress bindAddress, int poolSize) throws Exception
	{
		executor = Executors.newFixedThreadPool(poolSize);
		container = new HttpServer();
		server = new ContainerServer(container);
		connection = new SocketConnection(server);
		SocketAddress address = bindAddress;
		connection.connect(address);
	}

	public void stop() throws IOException
	{
		if (connection != null) connection.close();
	}

	@Override
	public void handle(Request request, Response response)
	{
		try
		{
			HttpTask task = new HttpTask(request, response, this);
			executor.execute(task);
		}
		catch (Exception e)
		{
			logger.error("Error in HTTP dispatch!", e);
		}
	}

	public void registerHttpRequestHandler(String path, IHttpRequestHandler handler)
	{
		HttpServer.httpRequestHandlers.put(path, handler);
	}

	public void registerHttpRequestACL(String path, String cidr)
	{
		List<String> acl = HttpServer.httpRequestACLs.get(path);
		if (acl == null)
		{
			acl = new ArrayList<>();
			acl.add(cidr);
			HttpServer.httpRequestACLs.put(path, acl);
		}
		else acl.add(cidr);
	}

	HashMap<String, IHttpRequestHandler> getHttpRequestHandlers()
	{
		return HttpServer.httpRequestHandlers;
	}

	HashMap<String, List<String>> getHttpRequestACLs()
	{
		return HttpServer.httpRequestACLs;
	}
}
