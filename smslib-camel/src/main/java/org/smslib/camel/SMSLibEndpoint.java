package org.smslib.camel;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.api.management.ManagedResource;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.smslib.Service;
import org.smslib.gateway.AbstractGateway;

/**
 * Represents a SMSLib endpoint.
 * The endpoint does not contain any logic but defines all the existing header IDs.
 * @author derjust
 */
@ManagedResource(description = "Managed SMSLibEndpoint")
@UriEndpoint(scheme = "smslib", consumerClass = SMSLibConsumer.class)
public abstract class SMSLibEndpoint extends DefaultEndpoint {
	/** Header ID of the body type itself (Text, binary) 
	 * @see org.smslib.message.Payload.Type */
	public static final String HEADER_BODY_TYPE = "BodyType";
	/** Header ID defining the source port of the SMS */
	public static final String HEADER_SOURCE_PORT = "SourcePort";
	/** Header ID defining the signature */
	public static final String HEADER_SIGNATURE = "Signature";
	/** Header ID defining the sent date of the message */
	public static final String HEADER_SENT_DATE = "SentDate";
	/** Header ID defining the recipient number of the message */
	public static final String HEADER_RECIPIENT_NUMBER = "RecipientNumber";
	/** Header ID defining the lexical representation of the recipient */
	public static final String HEADER_RECIPIENT = "Recipient";
	/** Header ID defining the originator number of the message */
	public static final String HEADER_ORIGINATOR_NUMBER = "OriginatorNumber";
	/** Header ID defining the lexical representation of the originator */
	public static final String HEADER_ORIGINATOR = "Originator";
	/** Header ID of the operator message ID of each message */
	public static final String HEADER_OPERATOR_MESSAGE_ID = "OperatorMessageId";
	/** Header ID of the technical ID of each message */
	public static final String HEADER_ID = "Id";
	/** Header ID defining the gateway id  */
	public static final String HEADER_GATEWAY_ID = "GatewayId";
	/** Header ID defining message's encoding 
	 * @see org.smslib.message.AbstractMessage.Encoding */
	public static final String HEADER_ENCODING = "Encoding";
	/** Header ID defining the destination port of the SMS */
	public static final String HEADER_DESTINATION_PORT = "DestinationPort";
	/** Header ID defining the type of the message itself (Inbound, Outbound, StatusReport) 
	 * @see org.smslib.message.AbstractMessage.Type */
	public static final String HEADER_TYPE = "Type";
	
	private Service service = Service.getInstance();

    public SMSLibEndpoint(String uri, SMSLibComponent component) {
        super(uri, component);
    }

    public SMSLibEndpoint(String endpointUri) {
        super(endpointUri);
    }

    public Producer createProducer() throws Exception {
        return new SMSLibProducer(this, getGateway());
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        return new SMSLibConsumer(this, processor, getGateway());
    }

    protected abstract AbstractGateway getGateway();

	public boolean isSingleton() {
        return true;
    }
    
    protected Service getService() {
		return service;
	}
    
    @Override
    protected void doSuspend() throws Exception {
    	super.doSuspend();
    }
    
    @Override
    protected void doResume() throws Exception {
    	super.doResume();
    }
    
    @Override
    protected void doStart() throws Exception {
    	super.doStart();
    	service.start();
    }

    @Override
    protected void doStop() throws Exception {
    	super.doStop();
    	service.stop();
    }
}
