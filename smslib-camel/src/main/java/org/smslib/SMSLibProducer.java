package org.smslib;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.gateway.AbstractGateway;
import org.smslib.message.OutboundMessage;

import static org.smslib.SMSLibEndpoint.*;

/**
 * The SMSLib producer.
 */
public class SMSLibProducer extends DefaultProducer {
    private static final Logger LOG = LoggerFactory.getLogger(SMSLibProducer.class);
	private AbstractGateway gateway;

    public SMSLibProducer(SMSLibEndpoint endpoint, AbstractGateway gateway) {
        super(endpoint);
        this.gateway = gateway;
    }

    public void process(Exchange exchange) throws Exception {
    	String recipient = exchange.getIn().getHeader(HEADER_RECIPIENT_NUMBER, String.class);
    	String message = exchange.getIn().getBody(String.class);
    	OutboundMessage outputMessage = new OutboundMessage(
    			recipient, 
    			message);
    	LOG.info("Send to {}: {}", recipient, message);
    	
		gateway.send(outputMessage);
    }

}
