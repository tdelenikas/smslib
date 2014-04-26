
package org.smslib.http.handlers;

import java.io.PrintStream;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.Service;
import org.smslib.core.Settings;
import org.smslib.gateway.AbstractGateway;
import org.smslib.http.IHttpRequestHandler;

public class StatusHandler implements IHttpRequestHandler
{
	static Logger logger = LoggerFactory.getLogger(StatusHandler.class);

	@Override
	public Status process(Request request, Response response)
	{
		PrintStream p = null;
		try
		{
			p = response.getPrintStream();
			//
			showHeader(p);
			//
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
			//
			showFooter(p);
			//
			return Status.OK;
		}
		catch (Exception e)
		{
			logger.error("Error!", e);
			return Status.INTERNAL_SERVER_ERROR;
		}
		finally
		{
			if (p != null) p.close();
		}
	}

	void showHeader(PrintStream p)
	{
		p.println("<html>");
		p.println("<head>");
		p.println("</head>");
		p.println("<body>");
	}

	void showFooter(PrintStream p)
	{
		p.println("<hr>");
		p.println("<p style='font-size:80%;'>");
		p.println(Settings.LIBRARY_COPYRIGHT.replaceAll("http://smslib.org", "<a href='http://smslib.org'>http://smslib.org</a>") + ", " + "Version <b>" + Settings.LIBRARY_VERSION + "</b></br>");
		p.println("Page generated @ " + new Date());
		p.println("</p>");
		p.println("</body>");
		p.println("</html>");
	}

	String formatUpTime(Date origin)
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
		long diffHours = TimeUnit.SECONDS.toHours(diffSeconds) - TimeUnit.DAYS.toHours(diffDays);
		long diffMinutes = TimeUnit.SECONDS.toMinutes(diffSeconds) - TimeUnit.DAYS.toMinutes(diffDays) - TimeUnit.HOURS.toMinutes(diffHours);
		return String.format("%dd, %dh, %dm", diffDays, diffHours, diffMinutes);
	}

	double getSendingRate(Date origin, int total)
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

	String formatStatus(AbstractGateway.Status status)
	{
		if (status == AbstractGateway.Status.Stopped) return String.format("<span>%s</span>", status);
		else if (status == AbstractGateway.Status.Started) return String.format("<span style='color:green;'>%s</span>", status);
		else if (status == AbstractGateway.Status.Error) return String.format("<span style='color:red;'>%s</span>", status);
		else return String.format("<span style='color:orange;'>%s</span>", status);
	}

	String formatStatus(Service.Status status)
	{
		if (status == Service.Status.Stopped) return String.format("<span style='color:red;'>%s</span>", status);
		else if (status == Service.Status.Started) return String.format("<span style='color:green;'>%s</span>", status);
		else return String.format("<span style='color:orange;'>%s</span>", status);
	}
}
