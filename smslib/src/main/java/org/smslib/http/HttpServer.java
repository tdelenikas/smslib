
package org.smslib.http;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.smslib.Service;
import org.smslib.core.Settings;
import org.smslib.gateway.AbstractGateway;
import org.smslib.helper.Common;
import org.smslib.helper.Log;

public class HttpServer implements Container
{
	Container container;

	Server server;

	Connection connection;

	static String[][] URIs = { { "/status", "handleStatusRequest", "1", "acl-status" } };

	@Override
	public void handle(Request request, Response response)
	{
		PrintStream output = null;
		try
		{
			String visitorIp = request.getClientAddress().getAddress().toString().substring(1);
			output = response.getPrintStream();
			response.setValue("Content-Type", "text/html");
			long time = System.currentTimeMillis();
			response.setDate("Date", time);
			response.setDate("Last-Modified", time);
			String path = request.getPath().toString();
			Log.getInstance().getLog().debug(String.format("http, ip:%s, path:%s", visitorIp, path));
			boolean processed = false;
			boolean aclCheck = false;
			for (int i = 0; i < URIs.length; i += 2)
			{
				if (URIs[i][0].equalsIgnoreCase(path))
				{
					if (URIs[i][3].equalsIgnoreCase("acl-status")) aclCheck = checkACL(InetAddress.getByName(visitorIp), Settings.httpServerACLStatus);
					if (aclCheck)
					{
						Log.getInstance().getLog().debug("HTTP/Invoking system handler: " + URIs[i][0]);
						if (URIs[i][2].equalsIgnoreCase("1")) showHeader(output);
						Method m = getClass().getMethod(URIs[i][1], PrintStream.class);
						m.invoke(this, output);
						if (URIs[i][2].equalsIgnoreCase("1")) showFooter(output);
						processed = true;
						break;
					}
				}
			}
			if (!processed)
			{
				for (String p : Service.getInstance().getHttpRequestHandlers().keySet())
				{
					if (p.equalsIgnoreCase(path))
					{
						Log.getInstance().getLog().debug("HTTP/Invoking registered handler: " + p);
						processed = true;
						Status s = Service.getInstance().getHttpRequestHandlers().get(p).process(request, response);
						response.setCode(s.getCode());
						response.setDescription(Status.getDescription(s.getCode()));
						break;
					}
				}
			}
			if (!aclCheck)
			{
				response.setCode(Status.FORBIDDEN.getCode());
				response.setDescription(Status.getDescription(Status.FORBIDDEN.getCode()));
				output.println("<h1>Forbidden: <i>" + path + "</i></h1>");
				showFooter(output);
			}
			else if (!processed)
			{
				response.setCode(Status.NOT_FOUND.getCode());
				response.setDescription(Status.getDescription(Status.NOT_FOUND.getCode()));
				output.println("<h1>Not found: <i>" + path + "</i></h1>");
				showFooter(output);
			}
		}
		catch (Exception e)
		{
			Log.getInstance().getLog().error("Unhandled HTTP Exception #1!", e);
		}
		finally
		{
			if (output != null)
			{
				output.close();
			}
			try
			{
				response.commit();
				response.close();
			}
			catch (Exception e)
			{
				Log.getInstance().getLog().error("Unhandled HTTP Exception #2!", e);
			}
		}
	}

	public void handleStatusRequest(PrintStream p)
	{
		p.println("<h2>SMSLib status</h2>");
		p.println("<h3>Service information</h3>");
		p.println("<table border='1' cellpadding='5' cellspacing='2'");
		p.println("<thead>");
		p.println("<tr><td>Status</td><td>Uptime</td><td>Callback Queue</td><td>Message Queue</td><td>Received</td><td>Sent</td><td>Failed</td><td>Failures</td><td>Sending Rate</td></tr>");
		p.println("<tbody>");
		p.println(String.format("<tr><td>%s</td><td>%s</td><td>%d</td><td>%d</td><td>%d</td><td>%d</td><td>%d</td><td>%d</td><td>%f m/s</td></tr>", formatStatus(Service.getInstance().getStatus()), formatUpTime(Service.getInstance().getStatistics().getStartTime()), Service.getInstance().getCallbackManager().getQueueLoad(), Service.getInstance().getAllQueueLoad(), Service.getInstance().getStatistics().getTotalReceived(), Service.getInstance().getStatistics().getTotalSent(), Service.getInstance().getStatistics().getTotalFailed(), Service.getInstance().getStatistics().getTotalFailures(), getSendingRate(Service.getInstance().getStatistics().getStartTime(), Service.getInstance().getStatistics().getTotalSent())));
		p.println("</table>");
		p.println("<h3>Gateway information</h3>");
		p.println("<table border='1' cellpadding='5' cellspacing='2'");
		p.println("<thead>");
		p.println("<tr><td>Gateway ID</td><td>Description</td><td>Class</td><td>Status</td><td>Uptime</td><td>Message Queue</td><td>Received</td><td>Sent</td><td>Failed</td><td>Failures</td><td>Sending Rate</td></tr>");
		p.println("<tbody>");
		for (String gId : Service.getInstance().getGatewayIDs())
		{
			p.println("<tr>");
			AbstractGateway g = Service.getInstance().getGatewayById(gId);
			p.println(String.format("<td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%d</td><td>%d</td><td>%d</td><td>%d</td><td>%d</td><td>%f m/s</td>", gId, g.getDescription(), g.getClass(), formatStatus(g.getStatus()), (g.getStatus() == AbstractGateway.Status.Started ? formatUpTime(g.getStatistics().getStartTime()) : "N/A"), g.getQueueLoad(), g.getStatistics().getTotalReceived(), g.getStatistics().getTotalSent(), g.getStatistics().getTotalFailed(), g.getStatistics().getTotalFailures(), getSendingRate(g.getStatistics().getStartTime(), g.getStatistics().getTotalSent())));
			p.println("</tr>");
		}
		p.println("</table>");
	}

	private void showHeader(PrintStream p)
	{
		p.println("<html>");
		p.println("<head>");
		p.println("</head>");
		p.println("<body>");
	}

	private void showFooter(PrintStream p)
	{
		p.println("<hr>");
		p.println("<p style='font-size:80%;'>");
		p.println(Settings.LIBRARY_COPYRIGHT.replaceAll("http://smslib.org", "<a href='http://smslib.org'>http://smslib.org</a>") + ", " + "Version <b>" + Settings.LIBRARY_VERSION + "</b></br>");
		p.println("Page generated @ " + new Date());
		p.println("</p>");
		p.println("</body>");
		p.println("</html>");
	}

	public void start() throws IOException
	{
		this.container = new HttpServer();
		this.server = new ContainerServer(container);
		this.connection = new SocketConnection(this.server);
		SocketAddress address = new InetSocketAddress(Settings.httpServerPort);
		this.connection.connect(address);
	}

	public void terminate() throws IOException
	{
		this.connection.close();
	}

	public String formatUpTime(Date origin)
	{
		if (origin == null) return "N/A";
		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		calendar1.setTime(origin);
		calendar2.setTime(new Date());
		long milliseconds1 = calendar1.getTimeInMillis();
		long milliseconds2 = calendar2.getTimeInMillis();
		long diff = milliseconds2 - milliseconds1;
		long diffSeconds = diff / 1000;
	
		long diffDays = (int) TimeUnit.SECONDS.toDays(diffSeconds);
		long diffHours = TimeUnit.SECONDS.toHours(diffSeconds) -
	                 TimeUnit.DAYS.toHours(diffDays);
		long diffMinutes = TimeUnit.SECONDS.toMinutes(diffSeconds) - 
	                  TimeUnit.DAYS.toMinutes(diffDays) -
	                  TimeUnit.HOURS.toMinutes(diffHours);
	    
		return String.format("%dd, %dh, %dm", diffDays, diffHours, diffMinutes);
	}

	public double getSendingRate(Date origin, int total)
	{
		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		calendar1.setTime(origin);
		calendar2.setTime(new Date());
		long milliseconds1 = calendar1.getTimeInMillis();
		long milliseconds2 = calendar2.getTimeInMillis();
		long diff = (milliseconds2 - milliseconds1) / 1000;
		return ((double) total / (double) diff);
	}

	public String formatStatus(AbstractGateway.Status status)
	{
		if (status == AbstractGateway.Status.Stopped) return String.format("<span>%s</span>", status);
		else if (status == AbstractGateway.Status.Started) return String.format("<span style='color:green;'>%s</span>", status);
		else if (status == AbstractGateway.Status.Error) return String.format("<span style='color:red;'>%s</span>", status);
		else return String.format("<span style='color:orange;'>%s</span>", status);
	}

	public String formatStatus(Service.Status status)
	{
		if (status == Service.Status.Stopped) return String.format("<span style='color:red;'>%s</span>", status);
		else if (status == Service.Status.Started) return String.format("<span style='color:green;'>%s</span>", status);
		else return String.format("<span style='color:orange;'>%s</span>", status);
	}

	public boolean checkACL(InetAddress ip, String aclCIDR) throws UnknownHostException
	{
		StringTokenizer tokens = new StringTokenizer(aclCIDR, ":");
		while (tokens.hasMoreTokens())
			if (Common.checkIPInCIDR(ip, tokens.nextToken())) return true;
		return false;
	}
}
