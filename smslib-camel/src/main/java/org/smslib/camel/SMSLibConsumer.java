package org.smslib.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.Service;
import org.smslib.callback.IInboundMessageCallback;
import org.smslib.callback.events.InboundMessageCallbackEvent;
import org.smslib.gateway.AbstractGateway;
import org.smslib.message.InboundMessage;

import static org.smslib.camel.SMSLibEndpoint.*;

/**
 * The SMSLib consumer.
 * The consumer maps all fields of the incomming message to headers in the exchange and preserves the incomming payload (String or Byte[]) as exchange's body.
 * @author derjust
 */
public class SMSLibConsumer extends DefaultConsumer {

	private static final Logger LOG = LoggerFactory.getLogger(SMSLibConsumer.class);

    private IInboundMessageCallback inboundMessageCallback = new IInboundMessageCallback() {

		/**
		 * Maps the incomming SMSlib event to a Camel exchange.
		 * @param event The SMSlib event which was received
		 * @return The created and filled exchange
		 */
		private Exchange mapMessage(InboundMessageCallbackEvent event) {
			Exchange exchange = getEndpoint().createExchange();

			InboundMessage inputMessage = event.getMessage();
			//Update the timesamp to "now"
	        exchange.setProperty(Exchange.RECEIVED_TIMESTAMP, (new java.util.Date()).getTime());
	        
	        Message outputMessage = exchange.getIn();
			outputMessage.setHeader(HEADER_BODY_TYPE, inputMessage.getPayload().getType().name());
			switch (inputMessage.getPayload().getType()) {
			case Text:
				//Encoding is expected to done already by SMSlib
				outputMessage.setBody(inputMessage.getPayload().getText());
				break;
			case Binary:
				outputMessage.setBody(inputMessage.getPayload().getBytes());
				break;
			default:
				LOG.warn("Unexpected payload type: {} - No boday availible", inputMessage.getPayload().getType());
				break;
			}
			
			//Map all fields to exchange headers
			outputMessage.setHeader(HEADER_TYPE, inputMessage.getType().name());
	        outputMessage.setHeader(HEADER_DESTINATION_PORT, inputMessage.getDestinationPort());
	        outputMessage.setHeader(HEADER_ENCODING, inputMessage.getEncoding().name());
	        outputMessage.setHeader(HEADER_GATEWAY_ID, inputMessage.getGatewayId());
	        outputMessage.setHeader(HEADER_ID, inputMessage.getId());
	        outputMessage.setHeader(HEADER_OPERATOR_MESSAGE_ID, inputMessage.getOperatorMessageId());
	        outputMessage.setHeader(HEADER_ORIGINATOR, inputMessage.getOriginatorAddress());
	        if (inputMessage.getOriginatorAddress() == null) {
	        	outputMessage.setHeader(HEADER_ORIGINATOR_NUMBER, inputMessage.getOriginatorAddress().getAddress());
	        }
	        outputMessage.setHeader(HEADER_RECIPIENT, inputMessage.getRecipientAddress());
	        if (inputMessage.getRecipientAddress() != null) {
	        	outputMessage.setHeader(HEADER_RECIPIENT_NUMBER, inputMessage.getRecipientAddress().getAddress());
	        }
	        outputMessage.setHeader(HEADER_SENT_DATE, inputMessage.getSentDate());
	        outputMessage.setHeader(HEADER_SIGNATURE, inputMessage.getSignature());
	        outputMessage.setHeader(HEADER_SOURCE_PORT, inputMessage.getSourcePort());
	        
			return exchange;
		}
		
		/** 
		 * Handler of SMSlib to process the incoming messages.
		 * Each event is mapped to a Camel exchange and any exception is propagated to Camel
		 * @param event The SMSlib event to process
		 * @see Exchange#setException(Throwable)
		 * @see org.smslib.callback.IInboundMessageCallback#process(org.smslib.callback.events.InboundMessageCallbackEvent)
		 */
		@Override
		public boolean process(InboundMessageCallbackEvent event) {
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
