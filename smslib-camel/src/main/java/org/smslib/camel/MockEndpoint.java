package org.smslib.camel;

import org.apache.camel.spi.UriPath;
import org.smslib.gateway.MockGateway;

/**
 * Wrapper around {@link MockGateway} for Camel.
 * Provides a delay and a failureRate at which messages are generated and delivered to the route.
 * 
 * @author derjust
 */
public class MockEndpoint extends SMSLibEndpoint {
	/**
	 * percent rate of failure. Any value > 0 is treated as "always".
	 * Details can be found in {@link org.smslib.gateway.MockGateway.failOperation()}
	 */
	@UriPath
	private int failureRate;
	/**
	 * Delay in ms between each message
	 */
	@UriPath
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
