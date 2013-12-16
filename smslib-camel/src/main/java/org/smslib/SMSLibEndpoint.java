package org.smslib;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.api.management.ManagedResource;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.smslib.gateway.AbstractGateway;

/**
 * Represents a SMSLib endpoint.
 */
@ManagedResource(description = "Managed SMSLibEndpoint")
@UriEndpoint(scheme = "smslib", consumerClass = SMSLibConsumer.class)
public abstract class SMSLibEndpoint extends DefaultEndpoint {
	public static final String HEADER_BODY_TYPE = "BodyType";
	public static final String HEADER_SOURCE_PORT = "SourcePort";
	public static final String HEADER_SIGNATURE = "Signature";
	public static final String HEADER_SENT_DATE = "SentDate";
	public static final String HEADER_RECIPIENT_NUMBER = "RecipientNumber";
	public static final String HEADER_RECIPIENT = "Recipient";
	public static final String HEADER_ORIGINATOR_NUMBER = "OriginatorNumber";
	public static final String HEADER_ORIGINATOR = "Originator";
	public static final String HEADER_OPERATOR_MESSAGE_ID = "OperatorMessageId";
	public static final String HEADER_ID = "Id";
	public static final String HEADER_GATEWAY_ID = "GatewayId";
	public static final String HEADER_ENCODING = "Encoding";
	public static final String HEADER_DESTINATION_PORT = "DestinationPort";
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
