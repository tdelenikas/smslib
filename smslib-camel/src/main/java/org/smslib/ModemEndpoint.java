package org.smslib;

import org.smslib.gateway.modem.Modem;

public class ModemEndpoint extends SMSLibEndpoint {

	public ModemEndpoint(String uri, SMSLibComponent component) {
		super(uri, component);
	}

	public ModemEndpoint(String endpointUri) {
		super(endpointUri);
	}

	@Override
	protected Modem getGateway() {
		return null;
	}

}
