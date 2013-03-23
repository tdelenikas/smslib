
package org.smslib.gateway.modem.driver;

import java.io.IOException;

import org.smslib.gateway.modem.Modem;
import org.smslib.gateway.modem.driver.serial.CommPortIdentifier;
import org.smslib.gateway.modem.driver.serial.SerialPort;
import org.smslib.gateway.modem.driver.serial.SerialPortEvent;
import org.smslib.gateway.modem.driver.serial.SerialPortEventListener;
import org.smslib.helper.Log;

public class SerialModemDriver extends AbstractModemDriver implements SerialPortEventListener
{
	String portName;

	int baudRate;

	CommPortIdentifier portId;

	SerialPort serialPort;


	public SerialModemDriver(Modem modem, String port, int baudRate)
	{
		super(modem);
		this.portName = port;
		this.baudRate = baudRate;
	}

	@Override
	public void openPort() throws NumberFormatException, IOException
	{
		Log.getInstance().getLog().debug("Opening comm port: " + getPortInfo());
		CommPortIdentifier.getPortIdentifiers();
		this.portId = CommPortIdentifier.getPortIdentifier(this.portName);
		this.serialPort = this.portId.open("org.smslib", 1971);
		this.in = this.serialPort.getInputStream();
		this.out = this.serialPort.getOutputStream();
		this.serialPort.setSerialPortParams(this.baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		this.serialPort.setInputBufferSize(Integer.valueOf(getModemSettings("port_buffer")));
		this.serialPort.setOutputBufferSize(Integer.valueOf(getModemSettings("port_buffer")));
		this.serialPort.enableReceiveThreshold(2);
		this.serialPort.enableReceiveTimeout(Integer.valueOf(getModemSettings("timeout")));
		this.serialPort.notifyOnDataAvailable(true);
		this.serialPort.addEventListener(this);
		this.serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
		this.pollReader = new PollReader();
		this.pollReader.start();
	}

	@Override
	public void closePort() throws IOException, InterruptedException
	{
		Log.getInstance().getLog().debug("Closing comm port: " + getPortInfo());
		this.pollReader.cancel();
		this.pollReader.join();
		this.in.close();
		this.in = null;
		this.out.close();
		this.out = null;
		this.serialPort.close();
	}

	@Override
	public String getPortInfo()
	{
		return this.portName + ":" + this.baudRate;
	}

	@Override
	public void serialEvent(SerialPortEvent event)
	{
		int eventType = event.getEventType();
		if (eventType == SerialPortEvent.DATA_AVAILABLE) this.pollReader.interrupt();
	}
}
