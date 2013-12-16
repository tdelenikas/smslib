package org.smslib;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.callback.IInboundMessageCallback;
import org.smslib.callback.events.InboundMessageEvent;
import org.smslib.gateway.AbstractGateway;
import org.smslib.message.InboundMessage;

import static org.smslib.SMSLibEndpoint.*;

/**
 * The SMSLib consumer.
 */
public class SMSLibConsumer extends DefaultConsumer {

	private static final Logger LOG = LoggerFactory.getLogger(SMSLibConsumer.class);

    private IInboundMessageCallback inboundMessageCallback = new IInboundMessageCallback() {

		private Exchange mapMessage(InboundMessageEvent event) {
			Exchange exchange = getEndpoint().createExchange();

			InboundMessage inputMessage = event.getMessage();
			
	        exchange.setProperty(Exchange.RECEIVED_TIMESTAMP, (new java.util.Date()).getTime());
	        
	        Message outputMessage = exchange.getIn();
			outputMessage.setHeader(HEADER_BODY_TYPE, inputMessage.getPayload().getType().name());
			switch (inputMessage.getPayload().getType()) {
			case Text:
				outputMessage.setBody(inputMessage.getPayload().getText());
				break;
			case Binary:
				outputMessage.setBody(inputMessage.getPayload().getBytes());
				break;
			default:
				LOG.warn("Unexpected payload type: {} - No boday availible", inputMessage.getPayload().getType());
				break;
			}
			
			outputMessage.setHeader(HEADER_TYPE, inputMessage.getType().name());
	        outputMessage.setHeader(HEADER_DESTINATION_PORT, inputMessage.getDestinationPort());
	        outputMessage.setHeader(HEADER_ENCODING, inputMessage.getEncoding().name());
	        outputMessage.setHeader(HEADER_GATEWAY_ID, inputMessage.getGatewayId());
	        outputMessage.setHeader(HEADER_ID, inputMessage.getId());
	        outputMessage.setHeader(HEADER_OPERATOR_MESSAGE_ID, inputMessage.getOperatorMessageId());
	        outputMessage.setHeader(HEADER_ORIGINATOR, inputMessage.getOriginator());
	        if (inputMessage.getOriginator() == null) {
	        	outputMessage.setHeader(HEADER_ORIGINATOR_NUMBER, inputMessage.getOriginator().getNumber());
	        }
	        outputMessage.setHeader(HEADER_RECIPIENT, inputMessage.getRecipient());
	        if (inputMessage.getRecipient() != null) {
	        	outputMessage.setHeader(HEADER_RECIPIENT_NUMBER, inputMessage.getRecipient().getNumber());
	        }
	        outputMessage.setHeader(HEADER_SENT_DATE, inputMessage.getSentDate());
	        outputMessage.setHeader(HEADER_SIGNATURE, inputMessage.getSignature());
	        outputMessage.setHeader(HEADER_SOURCE_PORT, inputMessage.getSourcePort());
	        
			return exchange;
		}
		
		@Override
		public boolean process(InboundMessageEvent event) {
			Exchange exchange = mapMessage(event);
	        try {
	            getProcessor().process(exchange);
	        } catch (Exception e) {
	            exchange.setException(e);
	        }

	        // handle any thrown exception
	        if (exchange.getException() != null) {
	            getExceptionHandler().handleException("Error processing exchange", exchange, exchange.getException());
	            return false;
	        }
	        return true;
		}
	};

    private AbstractGateway gateway;

	public SMSLibConsumer(SMSLibEndpoint endpoint, Processor processor, AbstractGateway gateway) {
        super(endpoint, processor);
        this.gateway = gateway; 
    }

    @Override
    protected void doResume() throws Exception {
    	super.doResume();

        getService().setInboundMessageCallback(inboundMessageCallback);
        getService().registerGateway(gateway);
    }
	
    @Override
    protected void doStart() throws Exception {
    	super.doStart();
    	
        getService().setInboundMessageCallback(inboundMessageCallback);
        getService().registerGateway(gateway);
    }
    
    @Override
    protected void doStop() throws Exception {
    	super.doStop();
    	
    	getService().unregisterGateway(gateway);
    	getService().setInboundMessageCallback(null);
    }
    
    @Override
    protected void doSuspend() throws Exception {
    	super.doSuspend();
    	
    	getService().unregisterGateway(gateway);
    	getService().setInboundMessageCallback(null);
    }

    private Service getService() {
    	return ((SMSLibEndpoint) super.getEndpoint()).getService();
    }

}
