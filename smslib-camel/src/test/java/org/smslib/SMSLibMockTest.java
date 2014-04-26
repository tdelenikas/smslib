package org.smslib;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

/**
 * Test case to verify that the bare minimum setup (SMSlib's mock interface) 
 * is working in Camel scenario.
 * 
 * @author derjust
 *
 */
public class SMSLibMockTest extends CamelTestSupport {

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
                from("smslib://mock?delay=1000")
                  .to("mock:result");
            }
        };
    }
}
