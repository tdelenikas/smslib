package org.smslib;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test case to test incoming SMS via SMSlib based serial port modem
 * 
 * @author derjust
 *
 */
@Ignore("Requires hardware to be present. Please check final fields and update for your local settings")
public class SMSLibModemTest extends CamelTestSupport {

	private static final String port = "COM5";
	private static final String baud = "19200";
	private static final String pin = "0000";
	private static final String smscNumber = "3097100000";

	@Test
	public void testSMSLib() throws Exception {
		MockEndpoint mock = getMockEndpoint("mock:result");
		mock.expectedMinimumMessageCount(1);

		assertMockEndpointsSatisfied();
	}

	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			public void configure() {
				from("smslib://modem?" //
						+ "address=" + port //
						+ "&port=" + baud //
						+ "&simPin=" + pin + "&simPin2=" + pin //
						+ "&smscNumber=" + smscNumber) //
						.to("mock:result");
			}
		};
	}
}
