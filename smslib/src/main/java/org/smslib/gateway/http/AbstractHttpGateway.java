
package org.smslib.gateway.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.core.Coverage;
import org.smslib.core.CreditBalance;
import org.smslib.gateway.AbstractGateway;
import org.smslib.message.DeliveryReportMessage;
import org.smslib.message.DeliveryReportMessage.DeliveryStatus;
import org.smslib.message.InboundMessage;
import org.smslib.message.OutboundMessage;
import org.smslib.message.OutboundMessage.SentStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public abstract class AbstractHttpGateway extends AbstractGateway
{
	static Logger logger = LoggerFactory.getLogger(AbstractHttpGateway.class);

	public enum HttpMethod
	{
		Undefined, POST, GET;
		@Override
		public String toString()
		{
			if (this == Undefined) return "Undefined";
			if (this == POST) return "POST";
			if (this == GET) return "GET";
			return "";
		}

		public static HttpMethod mapStringValue(String s)
		{
			if (s.equalsIgnoreCase("post")) return HttpMethod.POST;
			if (s.equalsIgnoreCase("get")) return HttpMethod.GET;
			return null;
		}
	}

	public enum Operation
	{
		None, SendMessage, QueryBalance, QueryMessage, QueryCoverage
	}

	HashMap<String, String> settings = new HashMap<>();

	public AbstractHttpGateway(String id, String description)
	{
		super(2, id, description);
		setHttpEncoding("UTF-8");
	}

	public AbstractHttpGateway(int concurrencyLevel, String id, String description)
	{
		super(concurrencyLevel, id, description);
		setHttpEncoding("UTF-8");
	}

	public AbstractHttpGateway(int noOfDispatchers, int concurrencyLevel, String id, String description)
	{
		super(noOfDispatchers, concurrencyLevel, id, description);
		setHttpEncoding("UTF-8");
	}

	public String getBaseUrl()
	{
		return this.settings.get("http-base-url");
	}

	public void setBaseUrl(String url)
	{
		this.settings.put("http-base-url", url);
	}

	public String getSubmitMessageUrl()
	{
		return getBaseUrl() + (this.settings.get("http-submit-message-url") == null ? "" : this.settings.get("http-submit-message-url"));
	}

	public void setSubmitMessageUrl(String url)
	{
		this.settings.put("http-submit-message-url", url);
	}

	public String getQueryMessageUrl()
	{
		return getBaseUrl() + (this.settings.get("http-query-message-url") == null ? "" : this.settings.get("http-query-message-url"));
	}

	public void setQueryMessageUrl(String url)
	{
		this.settings.put("http-query-message-url", url);
	}

	public String getQueryBalanceUrl()
	{
		return getBaseUrl() + (this.settings.get("http-query-balance-url") == null ? "" : this.settings.get("http-query-balance-url"));
	}

	public void setQueryBalanceUrl(String url)
	{
		this.settings.put("http-query-balance-url", url);
	}

	public String getQueryCoverageUrl()
	{
		return getBaseUrl() + (this.settings.get("http-query-coverage-url") == null ? "" : this.settings.get("http-query-coverage-url"));
	}

	public void setQueryCoverageUrl(String url)
	{
		this.settings.put("http-query-coverage-url", url);
	}

	public HttpMethod getHttpMethod()
	{
		return HttpMethod.mapStringValue(this.settings.get("http-method"));
	}

	public void setHttpMethod(HttpMethod httpMethod)
	{
		this.settings.put("http-method", httpMethod.toString());
	}

	public String getHttpEncoding()
	{
		return this.settings.get("http-encoding");
	}

	public void setHttpEncoding(String enc)
	{
		this.settings.put("http-encoding", enc);
	}

	protected Document loadXMLFromString(List<String> xmlLines) throws Exception
	{
		StringBuffer b = new StringBuffer(512);
		for (String l : xmlLines)
			b.append(l);
		return loadXMLFromString(b.toString().replaceAll("\\s\\s", " "));
	}

	protected Document loadXMLFromString(String xml) throws Exception
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xml));
		return builder.parse(is);
	}

	protected String getXMLTextValue(Element element, String tagName)
	{
		String textVal = null;
		NodeList nl = element.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0)
		{
			Element el = (Element) nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}
		return textVal;
	}

	protected double getXMLDoubleValue(Element element, String tagName)
	{
		return Double.parseDouble(getXMLTextValue(element, tagName));
	}

	protected int getXMLIntegerValue(Element element, String tagName)
	{
		return Integer.parseInt(getXMLTextValue(element, tagName));
	}

	public HttpMethod getOperationHttpMethod(Operation op)
	{
		return getHttpMethod();
	}

	@Override
	public void _start()
	{
		//Nothing here...
	}

	@Override
	public void _stop()
	{
		//Nothing here...
	}

	@Override
	public boolean _send(OutboundMessage message) throws Exception
	{
		Hashtable<String, String> parameters = new Hashtable<>();
		prepareParameters(Operation.SendMessage, message, parameters);
		parseResponse(Operation.SendMessage, message, performHttpRequest(getOperationHttpMethod(Operation.SendMessage), getSubmitMessageUrl(), parameters));
		return (message.getSentStatus() == SentStatus.Sent);
	}

	@Override
	protected boolean _delete(InboundMessage message)
	{
		return false;
	}

	@Override
	public CreditBalance _queryCreditBalance() throws Exception
	{
		Hashtable<String, String> parameters = new Hashtable<>();
		prepareParameters(Operation.QueryBalance, this, parameters);
		parseResponse(Operation.QueryBalance, this, performHttpRequest(getOperationHttpMethod(Operation.QueryBalance), getQueryBalanceUrl(), parameters));
		return getCreditBalance();
	}

	@Override
	public DeliveryStatus _queryDeliveryStatus(String operatorMessageId) throws Exception
	{
		DeliveryReportMessage dummyMessage = new DeliveryReportMessage();
		dummyMessage.setOperatorMessageId(operatorMessageId);
		Hashtable<String, String> parameters = new Hashtable<>();
		prepareParameters(Operation.QueryMessage, dummyMessage, parameters);
		parseResponse(Operation.QueryMessage, dummyMessage, performHttpRequest(getOperationHttpMethod(Operation.QueryMessage), getQueryMessageUrl(), parameters));
		return dummyMessage.getDeliveryStatus();
	}

	@Override
	public Coverage _queryCoverage(Coverage c) throws Exception
	{
		Hashtable<String, String> parameters = new Hashtable<>();
		prepareParameters(Operation.QueryCoverage, c, parameters);
		parseResponse(Operation.QueryCoverage, c, performHttpRequest(getOperationHttpMethod(Operation.QueryCoverage), getQueryCoverageUrl(), parameters));
		return c;
	}

	protected List<String> performHttpRequest(HttpMethod method, String url, Hashtable<String, String> parameters) throws IOException
	{
		URL u;
		List<String> responseList = new ArrayList<>();
		URLConnection con;
		String line;
		BufferedReader in;
		switch (method)
		{
			case POST:
				u = new URL(url);
				OutputStreamWriter out;
				StringBuffer req;
				con = u.openConnection();
				con.setConnectTimeout(5000);
				con.setDoInput(true);
				con.setDoOutput(true);
				con.setUseCaches(false);
				con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				prepareUrlConnection(con);
				out = new OutputStreamWriter(con.getOutputStream());
				req = new StringBuffer(256);
				for (Enumeration<String> e = parameters.keys(); e.hasMoreElements();)
				{
					String k = e.nextElement();
					if (req.length() > 0) req.append("&");
					req.append(k);
					req.append('=');
					req.append(URLEncoder.encode(parameters.get(k), getHttpEncoding()));
				}
				logger.debug("HTTP POST -> " + url + " := " + req.toString());
				out.write(req.toString());
				out.flush();
				out.close();
				in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
				while ((line = in.readLine()) != null)
				{
					logger.debug("HTTP POST / Response -> " + line);
					responseList.add(line);
				}
				in.close();
				return responseList;
			case GET:
				u = new URL(url);
				con = u.openConnection();
				con.setConnectTimeout(5000);
				prepareUrlConnection(con);
				logger.debug("HTTP GET -> " + url);
				in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
				while ((line = in.readLine()) != null)
				{
					logger.debug("HTTP GET / Response -> " + line);
					responseList.add(line);
				}
				in.close();
				return responseList;
			default:
				throw new RuntimeException("Internal error - unknown HTTP method!");
		}
	}

	abstract protected void prepareUrlConnection(URLConnection con);

	abstract protected void prepareParameters(Operation operation, Object o, Hashtable<String, String> responseList, Object... args);

	abstract protected void parseResponse(Operation operation, Object o, List<String> responseList) throws Exception;

	abstract protected String translateText(String text);
}
