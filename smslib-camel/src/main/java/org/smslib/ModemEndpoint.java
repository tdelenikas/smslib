package org.smslib;

import org.apache.camel.spi.UriParam;
import org.smslib.gateway.modem.Modem;

public class ModemEndpoint extends SMSLibEndpoint {
	/** IP Address to a bulk provider or COM port */
	@UriParam
	private String address;
	/** Port to a bulk provider or COM baud rate */
	@UriParam
	private String port;
	@UriParam
	private String simPin;
	@UriParam
	private String simPin2;
	@UriParam
	private String smscNumber;
	@UriParam
	private String memoryLocations;

	public ModemEndpoint(String uri, SMSLibComponent component) {
		super(uri, component);
	}

	public ModemEndpoint(String endpointUri) {
		super(endpointUri);
	}

	@Override
	protected Modem getGateway() 
	{
		return new Modem(this.getEndpointKey(), address, port, simPin, simPin2, smscNumber, memoryLocations);
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the port
	 */
	public String getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(String port) {
		this.port = port;
	}

	/**
	 * @return the simPin
	 */
	public String getSimPin() {
		return simPin;
	}

	/**
	 * @param simPin the simPin to set
	 */
	public void setSimPin(String simPin) {
		this.simPin = simPin;
	}

	/**
	 * @return the simPin2
	 */
	public String getSimPin2() {
		return simPin2;
	}

	/**
	 * @param simPin2 the simPin2 to set
	 */
	public void setSimPin2(String simPin2) {
		this.simPin2 = simPin2;
	}

	/**
	 * @return the smscNumber
	 */
	public String getSmscNumber() {
		return smscNumber;
	}

	/**
	 * @param smscNumber the smscNumber to set
	 */
	public void setSmscNumber(String smscNumber) {
		this.smscNumber = smscNumber;
	}

	/**
	 * @return the memoryLocations
	 */
	public String getMemoryLocations() {
		return memoryLocations;
	}

	/**
	 * @param memoryLocations the memoryLocations to set
	 */
	public void setMemoryLocations(String memoryLocations) {
		this.memoryLocations = memoryLocations;
	}

}
