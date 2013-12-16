package org.smslib;

import org.smslib.gateway.MockGateway;

public class MockEndpoint extends SMSLibEndpoint {

	private int failureRate;
	private int delay;

	public MockEndpoint(String uri, SMSLibComponent component) {
		super(uri, component);
	}

	public MockEndpoint(String endpointUri) {
		super(endpointUri);
	}

	@Override
	protected MockGateway getGateway() {
		return new MockGateway(getEndpointKey(), getDelay(), getFailureRate());
	}

	/**
	 * @return the failureRate
	 */
	public int getFailureRate() {
		return failureRate;
	}

	/**
	 * @param failureRate the failureRate to set
	 */
	public void setFailureRate(int failureRate) {
		this.failureRate = failureRate;
	}

	/**
	 * @return the delay
	 */
	public int getDelay() {
		return delay;
	}

	/**
	 * @param delay the delay to set
	 */
	public void setDelay(int delay) {
		this.delay = delay;
	}

	
}
