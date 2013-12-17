package org.smslib;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class SMSLibModemTest extends CamelTestSupport {

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
                from("smslib://modem?address=COM8&port=19200&simPin=0000&simPin2=0000&smscNumber=3097100000")
                  .to("mock:result");
            }
        };
    }
}
