
package org.smslib.gateway.modem.driver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.Service;
import org.smslib.core.Capabilities;
import org.smslib.core.Capabilities.Caps;
import org.smslib.gateway.modem.DeviceInformation.Modes;
import org.smslib.gateway.modem.Modem;
import org.smslib.gateway.modem.ModemResponse;
import org.smslib.helper.Common;
import org.smslib.message.MsIsdn;

public abstract class AbstractModemDriver
{
	static Logger logger = LoggerFactory.getLogger(AbstractModemDriver.class);

	public Object _LOCK_ = new Object();

	Properties modemProperties;

	InputStream in = null;

	OutputStream out = null;

	StringBuffer buffer = new StringBuffer(4096);

	PollReader pollReader;

	Modem modem;

	boolean responseOk;

	String memoryLocations = "";

	int atATHCounter = 0;

	Pattern rxCallerId = Pattern.compile("\\p{Punct}CLIP\\s*:\\s*(\"\\p{Punct}*\\d*\").*");

	Pattern rxSupportedEncodings = Pattern.compile("\\+CSCS:\\s+(.*)");

	public abstract void openPort() throws IOException, TimeoutException, InterruptedException;

	public abstract void closePort() throws IOException, TimeoutException, InterruptedException;

	public abstract String getPortInfo();

	public AbstractModemDriver(Modem modem)
	{
		modemProperties = new Properties();
		try
		{
			InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("modem.properties");
			modemProperties.load(inputStream);
			inputStream.close();
		}
		catch (Exception e)
		{
			logger.error("Error!", e);
			throw new RuntimeException(e);
		}
		this.modem = modem;
	}

	public ModemResponse write(String data) throws IOException, TimeoutException, NumberFormatException, InterruptedException
	{
		return write(data, false);
	}

	public ModemResponse write(String data, boolean skipResponse) throws IOException, TimeoutException, NumberFormatException, InterruptedException
	{
		synchronized (this._LOCK_)
		{
			logger.debug(getPortInfo() + " <== " + data);
			write(data.getBytes());
			Common.countSheeps(Integer.valueOf(getModemSettings("command_wait_unit")));
			return (new ModemResponse((skipResponse ? "" : getResponse()), (skipResponse ? true : this.responseOk)));
		}
	}

	protected boolean hasData() throws IOException
	{
		return ((this.in != null) && (this.in.available() > 0));
	}

	protected int read() throws IOException
	{
		return this.in.read();
	}

	protected void write(byte[] s) throws IOException, NumberFormatException
	{
		int charDelay = Integer.valueOf(getModemSettings("char_wait_unit"));
		if (charDelay == 0) this.out.write(s);
		else
		{
			for (int i = 0; i < s.length; i++)
			{
				byte b = s[i];
				this.out.write(b);
				Common.countSheeps(charDelay);
			}
		}
	}

	protected void write(byte s) throws IOException
	{
		this.out.write(s);
	}

	private String getResponse() throws TimeoutException, IOException, NumberFormatException, InterruptedException
	{
		StringBuffer raw = new StringBuffer(256);
		StringBuffer b = new StringBuffer(256);
		while (true)
		{
			String line = getLineFromBuffer();
			logger.debug(getPortInfo() + " >>> " + line);
			this.buffer.delete(0, line.length() + 2);
			if (Common.isNullOrEmpty(line)) continue;
			if (line.charAt(0) == '^') continue;
			if (line.charAt(0) == '*') continue;
			if (line.startsWith("RING")) continue;
			if (line.startsWith("+STIN:")) continue;
			if (Integer.valueOf(getModemSettings("cpin_without_ok")) == 1)
			{
				if (line.startsWith("+CPIN:"))
				{
					raw.append(line);
					raw.append("$");
					b.append(line);
					this.responseOk = true;
					break;
				}
			}
			if (line.startsWith("+CLIP:"))
			{
				write("+++", true);
				Common.countSheeps(Integer.valueOf(getModemSettings("wait_unit")));
				write("ATH\r", true);
				logger.debug("+++ INCREASE ATH");
				this.atATHCounter++;
				String callerId = "";
				Matcher m = this.rxCallerId.matcher(line);
				if (m.find()) callerId = m.group(1).replaceAll("\"", "");
				Service.getInstance().getCallbackManager().registerInboundCallEvent(new MsIsdn(callerId), this.modem.getGatewayId());
				Common.countSheeps(Integer.valueOf(getModemSettings("wait_unit")));
				continue;
			}
			if (line.indexOf("OK") == 0)
			{
				if (this.atATHCounter > 0)
				{
					logger.debug("--- DECREASE ATH");
					this.atATHCounter--;
					continue;
				}
				this.responseOk = true;
				break;
			}
			if ((line.indexOf("ERROR") == 0) || (line.indexOf("+CMS ERROR") == 0) || (line.indexOf("+CME ERROR") == 0))
			{
				logger.error(getPortInfo() + " ERR==> " + line);
				this.responseOk = false;
				break;
			}
			if (b.length() > 0) b.append('\n');
			raw.append(line);
			raw.append("$");
			b.append(line);
		}
		logger.debug(getPortInfo() + " ==> " + raw.toString());
		return b.toString();
	}

	private String getLineFromBuffer() throws IOException, TimeoutException, NumberFormatException
	{
		long startTimeout = System.currentTimeMillis();
		long endTimeout = startTimeout;
		while (this.buffer.indexOf("\r") == -1)
		{
			endTimeout += Integer.valueOf(getModemSettings("wait_unit"));
			if ((endTimeout - startTimeout) > Integer.valueOf(getModemSettings("timeout"))) throw new TimeoutException("Timeout elapsed for " + getPortInfo());
			Common.countSheeps(Integer.valueOf(getModemSettings("wait_unit")));
		}
		BufferedReader r = new BufferedReader(new StringReader(this.buffer.toString()));
		String line = r.readLine();
		r.close();
		return line;
	}

	public void clearResponses() throws NumberFormatException, IOException
	{
		Common.countSheeps(Integer.valueOf(getModemSettings("wait_unit")) * 1);
		while (this.buffer.length() > 0)
		{
			this.buffer.delete(0, this.buffer.length());
			Common.countSheeps(Integer.valueOf(getModemSettings("wait_unit")) * 1);
		}
	}

	public void setMemoryLocations(String memoryLocations)
	{
		this.memoryLocations = memoryLocations;
	}

	public String getMemoryLocations()
	{
		return this.memoryLocations;
	}

	public class ClipReader extends Thread
	{
		@Override
		public void run()
		{
			try
			{
				Thread.sleep(1000);
				atATWithResponse();
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
			}
		}
	}

	public class PollReader extends Thread
	{
		private boolean shouldCancel = false;

		private boolean foundClip = false;

		public void cancel()
		{
			this.shouldCancel = true;
			this.interrupt();
		}

		@Override
		public void run()
		{
			logger.debug("Started!");
			while (!this.shouldCancel)
			{
				try
				{
					while (hasData())
					{
						char c = (char) read();
						//logger.debug("> " + c);
						AbstractModemDriver.this.buffer.append(c);
						if (AbstractModemDriver.this.buffer.indexOf("+CLIP") >= 0)
						{
							if (!this.foundClip)
							{
								this.foundClip = true;
								new ClipReader().start();
							}
						}
						else this.foundClip = false;
					}
				}
				catch (Exception e)
				{
					logger.error(e.getMessage(), e);
				}
				try
				{
					Common.countSheeps(Integer.valueOf(getModemSettings("poll_reader")));
				}
				catch (Exception e)
				{
					logger.error(e.getMessage(), e);
				}
			}
			logger.debug("Stopped!");
		}
	}

	public void initializeModem() throws Exception
	{
		int counter = 0;
		synchronized (this._LOCK_)
		{
			atAT();
			atAT();
			atAT();
			atAT();
			atEchoOff();
			clearResponses();
			this.modem.getDeviceInformation().setManufacturer(atGetManufacturer().getResponseData());
			this.modem.getDeviceInformation().setModel(atGetModel().getResponseData());
			Common.countSheeps(Integer.valueOf(getModemSettings("wait_unit")));
			atFromModemSettings("init1");
			Common.countSheeps(Integer.valueOf(getModemSettings("wait_unit")) * Integer.valueOf(getModemSettings("delay_after_init1")));
			atFromModemSettings("init2");
			Common.countSheeps(Integer.valueOf(getModemSettings("wait_unit")) * Integer.valueOf(getModemSettings("delay_after_init2")));
			clearResponses();
			atEchoOff();
			clearResponses();
			atFromModemSettings("pre_pin");
			Common.countSheeps(Integer.valueOf(getModemSettings("wait_unit")) * Integer.valueOf(getModemSettings("delay_after_pre_pin")));
			while (true)
			{
				counter++;
				if (counter == 5) throw new IOException("Modem does not correspond correctly, giving up...");
				ModemResponse simStatus = atGetSimStatus();
				if (simStatus.getResponseData().indexOf("SIM PIN2") >= 0)
				{
					if (Common.isNullOrEmpty(this.modem.getSimPin2())) throw new IOException("SIM PIN2 requested but not defined!", null);
					atEnterPin(this.modem.getSimPin2());
				}
				else if (simStatus.getResponseData().indexOf("SIM PIN") >= 0)
				{
					if (Common.isNullOrEmpty(this.modem.getSimPin())) throw new IOException("SIM PIN requested but not defined!", null);
					atEnterPin(this.modem.getSimPin());
				}
				else if (simStatus.getResponseData().indexOf("READY") >= 0) break;
				else if (simStatus.getResponseData().indexOf("OK") >= 0) break;
				else if (simStatus.getResponseData().indexOf("ERROR") >= 0)
				{
					logger.error("SIM PIN error!");
				}
				logger.info("SIM PIN Not ok, waiting for a while...");
				Common.countSheeps(Integer.valueOf(getModemSettings("wait_unit")) * Integer.valueOf(getModemSettings("delay_on_sim_error")));
			}
			atFromModemSettings("post_pin");
			Common.countSheeps(Integer.valueOf(getModemSettings("wait_unit")) * Integer.valueOf(getModemSettings("delay_after_post_pin")));
			atEnableClip();
			if (!atNetworkRegistration().isResponseOk()) throw new IOException("Network registration failed!", null);
			atVerboseOff();
			if (atSetPDUMode().isResponseOk()) this.modem.getDeviceInformation().setMode(Modes.PDU);
			else
			{
				logger.warn("Modem does not support PDU, trying to switch to TEXT...");
				if (atSetTEXTMode().isResponseOk())
				{
					Capabilities caps = new Capabilities();
					caps.set(Caps.CanSendMessage);
					this.modem.setCapabilities(caps);
					this.modem.getDeviceInformation().setMode(Modes.TEXT);
					ModemResponse encodings = atGetSupportedEncodings();
					String encSet = encodings.getResponseData();
					Matcher m = this.rxSupportedEncodings.matcher(encodings.getResponseData());
					encSet = m.group(1);
					encSet = encSet.replace("(", "");
					encSet = encSet.replace(")", "");
					encSet = encSet.replace("\"", "");
					StringTokenizer tokens = new StringTokenizer(encSet, ",");
					while (tokens.hasMoreTokens())
						this.modem.getDeviceInformation().getSupportedEncodings().add(tokens.nextToken());
				}
				else throw new IOException("Neither PDU nor TEXT mode are supported by this modem!");
			}
			atCnmiOff();
			retrieveMemoryLocations();
			refreshDeviceInformation();
		}
	}

	public void refreshDeviceInformation() throws Exception
	{
		this.modem.getDeviceInformation().setManufacturer(atGetManufacturer().getResponseData());
		this.modem.getDeviceInformation().setModel(atGetModel().getResponseData());
		this.modem.getDeviceInformation().setSerialNo(atGetSerialNo().getResponseData());
		this.modem.getDeviceInformation().setImsi(atGetImsi().getResponseData());
		this.modem.getDeviceInformation().setSwVersion(atGetSWVersion().getResponseData());
		String s = atGetSignalStrengh().getResponseData();
		if (this.responseOk)
		{
			String s1 = s.substring(s.indexOf(':') + 1).trim();
			StringTokenizer tokens = new StringTokenizer(s1, ",");
			int rssi = Integer.valueOf(tokens.nextToken().trim());
			this.modem.getDeviceInformation().setRssi(rssi == 99 ? 99 : (-113 + 2 * rssi));
		}
	}

	void retrieveMemoryLocations() throws IOException
	{
		if (Common.isNullOrEmpty(this.memoryLocations))
		{
			this.memoryLocations = getModemSettings("memory_locations");
			if (Common.isNullOrEmpty(this.memoryLocations)) this.memoryLocations = "";
			if (Common.isNullOrEmpty(this.memoryLocations))
			{
				try
				{
					String response = atGetMemoryLocations().getResponseData();
					if (response.indexOf("+CPMS:") >= 0)
					{
						int i, j;
						i = response.indexOf('(');
						while (response.charAt(i) == '(')
							i++;
						j = i;
						while (response.charAt(j) != ')')
							j++;
						response = response.substring(i, j);
						StringTokenizer tokens = new StringTokenizer(response, ",");
						while (tokens.hasMoreTokens())
						{
							String loc = tokens.nextToken().replaceAll("\"", "");
							if ((!loc.equalsIgnoreCase("MT")) && ((this.memoryLocations.indexOf(loc) < 0))) this.memoryLocations += loc;
						}
					}
					else
					{
						this.memoryLocations = "SM";
						logger.warn("CPMS detection failed, proceeding with default memory 'SM'.");
					}
				}
				catch (Exception e)
				{
					this.memoryLocations = "SM";
					logger.warn("CPMS detection failed, proceeding with default memory 'SM'.", e);
				}
			}
		}
		else logger.info("Using given memory locations: " + this.memoryLocations);
	}

	public String getSignature(boolean complete)
	{
		String manufacturer = this.modem.getDeviceInformation().getManufacturer().toLowerCase().replaceAll(" ", "").replaceAll(" ", "").replaceAll(" ", "");
		String model = this.modem.getDeviceInformation().getModel().toLowerCase().replaceAll(" ", "").replaceAll(" ", "").replaceAll(" ", "");
		return (complete ? manufacturer + "_" + model : manufacturer);
	}

	protected ModemResponse atAT() throws Exception
	{
		return write("AT\r", true);
	}

	protected ModemResponse atATWithResponse() throws Exception
	{
		return write("AT\r");
	}

	protected ModemResponse atEchoOff() throws Exception
	{
		return write("ATE0\r", true);
	}

	protected ModemResponse atGetSimStatus() throws Exception
	{
		return write("AT+CPIN?\r");
	}

	protected ModemResponse atEnterPin(String pin) throws Exception
	{
		return write(String.format("AT+CPIN=\"%s\"\r", pin));
	}

	protected ModemResponse atNetworkRegistration() throws Exception
	{
		write("AT+CREG=1\r");
		Common.countSheeps(Integer.valueOf(getModemSettings("wait_unit")) * Integer.valueOf(getModemSettings("delay_network_registration")));
		return write("AT+CREG?\r");
	}

	protected ModemResponse atEnableClip() throws Exception
	{
		return write("AT+CLIP=1\r");
	}

	protected ModemResponse atVerboseOff() throws Exception
	{
		return write("AT+CMEE=0\r");
	}

	protected ModemResponse atSetPDUMode() throws Exception
	{
		return write("AT+CMGF=0\r");
	}

	protected ModemResponse atSetTEXTMode() throws Exception
	{
		return write("AT+CMGF=1\r");
	}

	protected ModemResponse atCnmiOff() throws Exception
	{
		return write("AT+CNMI=2,0,0,0,0\r");
	}

	protected ModemResponse atGetManufacturer() throws Exception
	{
		return write("AT+CGMI\r");
	}

	protected ModemResponse atGetModel() throws Exception
	{
		return write("AT+CGMM\r");
	}

	protected ModemResponse atGetImsi() throws Exception
	{
		return write("AT+CIMI\r");
	}

	protected ModemResponse atGetSerialNo() throws Exception
	{
		return write("AT+CGSN\r");
	}

	protected ModemResponse atGetSWVersion() throws Exception
	{
		return write("AT+CGMR\r");
	}

	protected ModemResponse atGetSignalStrengh() throws Exception
	{
		return write("AT+CSQ\r");
	}

	public int atSendPDUMessage(int size, String pdu) throws Exception
	{
		write(String.format("AT+CMGS=%d\r", size), true);
		while (this.buffer.length() == 0)
			Common.countSheeps(Integer.valueOf(getModemSettings("wait_unit")));
		Common.countSheeps(Integer.valueOf(getModemSettings("wait_unit")) * Integer.valueOf(getModemSettings("delay_before_send_pdu")));
		clearResponses();
		write(pdu, true);
		write((byte) 26);
		String response = getResponse();
		if (this.responseOk) return Integer.parseInt(response.substring(response.indexOf(":") + 1).trim());
		return -1;
	}

	public int atSendTEXTMessage(String recipient, String text) throws Exception
	{
		write(String.format("AT+CSCS=\"%s\"\r", this.modem.getDeviceInformation().getEncoding()), true);
		if (!this.responseOk) throw new Exception("Unsupported encoding: " + this.modem.getDeviceInformation().getEncoding());
		write(String.format("AT+CMGS=\"%s\"\r", recipient), true);
		while (this.buffer.length() == 0)
			Common.countSheeps(Integer.valueOf(getModemSettings("wait_unit")));
		Common.countSheeps(Integer.valueOf(getModemSettings("wait_unit")) * Integer.valueOf(getModemSettings("delay_before_send_pdu")));
		clearResponses();
		write(text, true);
		write((byte) 26);
		String response = getResponse();
		if (this.responseOk) return Integer.parseInt(response.substring(response.indexOf(":") + 1).trim());
		return -1;
	}

	public ModemResponse atSetEncoding(String encoding) throws Exception
	{
		return write(String.format("AT+CSCS=\"%s\"\r", encoding));
	}

	public ModemResponse atGetSupportedEncodings() throws Exception
	{
		return write("AT+CSCS=?\r");
	}

	public ModemResponse atGetMemoryLocations() throws Exception
	{
		return write("AT+CPMS=?\r");
	}

	public ModemResponse atSwitchMemoryLocation(String memoryLocation) throws Exception
	{
		return write(String.format("AT+CPMS=\"%s\"\r", memoryLocation));
	}

	public ModemResponse atGetMessages(String memoryLocation) throws Exception
	{
		if (atSwitchMemoryLocation(memoryLocation).isResponseOk()) return (this.modem.getDeviceInformation().getMode() == Modes.PDU ? write("AT+CMGL=4\r") : write("AT+CMGL=\"ALL\"\r"));
		return new ModemResponse("", false);
	}

	public ModemResponse atDeleteMessage(String memoryLocation, int memoryIndex) throws Exception
	{
		if (atSwitchMemoryLocation(memoryLocation).isResponseOk()) return write(String.format("AT+CMGD=%d\r", memoryIndex));
		return new ModemResponse("", false);
	}

	public ModemResponse atFromModemSettings(String key) throws Exception
	{
		String atCommand = getModemSettings(key);
		if (!Common.isNullOrEmpty(atCommand)) return write(atCommand);
		return new ModemResponse("", true);
	}

	public String getModemSettings(String key) throws IOException
	{
		String fullSignature = getSignature(true);
		String shortSignature = getSignature(false);
		String value = "";
		if (!Common.isNullOrEmpty(fullSignature)) value = modemProperties.getProperty(fullSignature + "." + key);
		if ((Common.isNullOrEmpty(value)) && (!Common.isNullOrEmpty(shortSignature))) value = modemProperties.getProperty(shortSignature + "." + key);
		if (Common.isNullOrEmpty(value)) value = modemProperties.getProperty("default" + "." + key);
		return (Common.isNullOrEmpty(value) ? "" : value);
	}
}
