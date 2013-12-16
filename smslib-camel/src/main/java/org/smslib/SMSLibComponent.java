package org.smslib;

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.UriEndpointComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the component that manages {@link SMSLibEndpoint}.
 */
public class SMSLibComponent extends UriEndpointComponent {
	public SMSLibComponent() {
		super(SMSLibEndpoint.class);
	}

	static Logger logger = LoggerFactory.getLogger(SMSLibComponent.class);

    protected Endpoint createEndpoint(String uri, String path, Map<String, Object> parameters) throws Exception {
    	SMSLibEndpoint endpoint;
    	switch (path) {
    	case "mock":
    		endpoint = new MockEndpoint(uri, this);
    		break;
    	case "modem":
    		endpoint = new ModemEndpoint(uri, this);
    		break;
		default:
			throw new IllegalArgumentException(path + " not definied!");
    	
    	}
        setProperties(endpoint, parameters);
        return endpoint;
    }
    
}
