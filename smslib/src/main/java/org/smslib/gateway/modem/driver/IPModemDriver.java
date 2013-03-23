
package org.smslib.gateway.modem.driver;

import java.io.IOException;
import java.net.Socket;

import org.smslib.gateway.modem.Modem;
import org.smslib.helper.Common;
import org.smslib.helper.Log;

public class IPModemDriver extends AbstractModemDriver
{
	String address;

	int port;

	Socket socket;

	public IPModemDriver(Modem modem, String address, int port)
	{
		super(modem);
		this.address = address;
		this.port = port;
	}

	@Override
	public void openPort() throws IOException, NumberFormatException
	{
		Log.getInstance().getLog().debug("Opening IP port: " + getPortInfo());
		this.socket = new Socket(this.address, this.port);
		this.socket.setReceiveBufferSize(Integer.valueOf(getModemSettings("port_buffer")));
		this.socket.setSendBufferSize(Integer.valueOf(getModemSettings("port_buffer")));
		this.socket.setSoTimeout(30000);
		this.socket.setTcpNoDelay(true);
		this.in = this.socket.getInputStream();
		this.out = this.socket.getOutputStream();
		Common.countSheeps(Integer.valueOf(getModemSettings("after_ip_connect_wait_unit")));
		this.pollReader = new PollReader();
		this.pollReader.start();
	}

	@Override
	public void closePort() throws IOException, InterruptedException
	{
		Log.getInstance().getLog().debug("Closing IP port: " + getPortInfo());
		this.pollReader.cancel();
		this.pollReader.join();
		this.in.close();
		this.in = null;
		this.out.close();
		this.out = null;
		this.socket.close();
		Common.countSheeps(Integer.valueOf(getModemSettings("after_ip_connect_wait_unit")));
	}

	@Override
	public String getPortInfo()
	{
		return this.address + ":" + this.port;
	}
}
