
package org.smslib.gateway.smpp;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.core.Capabilities;
import org.smslib.core.Capabilities.Caps;
import org.smslib.core.Coverage;
import org.smslib.core.CreditBalance;
import org.smslib.gateway.AbstractGateway;
import org.smslib.message.DeliveryReportMessage.DeliveryStatus;
import org.smslib.message.InboundMessage;
import org.smslib.message.MsIsdn;
import org.smslib.message.OutboundMessage;
import org.smslib.message.OutboundMessage.FailureCause;
import org.smslib.message.OutboundMessage.SentStatus;
import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppClient;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.EnquireLink;
import com.cloudhopper.smpp.pdu.EnquireLinkResp;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.cloudhopper.smpp.type.Address;

public class Smpp extends AbstractGateway
{
	static Logger logger = LoggerFactory.getLogger(Smpp.class);

	String hostAddress;

	int hostPort;

	String username;

	String password;

	int timeout = 5000;

	SmppSession smppSession;

	SmppSessionConfiguration smppConfig;

	EnquireLinkResp enquireLinkResp;

	ScheduledThreadPoolExecutor monitorExecutor;

	DefaultSmppClient clientBootstrap;

	DefaultSmppSessionHandler sessionHandler;

	public Smpp(String gatewayId, String address, int port, String username, String password)
	{
		super(1, gatewayId, "SMPP");
		Capabilities caps = new Capabilities();
		caps.set(Caps.CanSendMessage);
		caps.set(Caps.CanSendUnicodeMessage);
		caps.set(Caps.CanSetSenderId);
		setCapabilities(caps);
		this.hostAddress = address;
		this.hostPort = port;
		this.username = username;
		this.password = password;
	}

	@Override
	protected void _start() throws Exception
	{
		this.monitorExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1, new ThreadFactory()
		{
			private AtomicInteger sequence = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable r)
			{
				Thread t = new Thread(r);
				t.setName("SmppClientSessionWindowMonitorPool-" + this.sequence.getAndIncrement());
				return t;
			}
		});
		this.clientBootstrap = new DefaultSmppClient(Executors.newCachedThreadPool(), 1, this.monitorExecutor);
		this.sessionHandler = new ClientSmppSessionHandler();
		this.smppConfig = new SmppSessionConfiguration();
		this.smppConfig.setWindowSize(1);
		this.smppConfig.setName("SMSLib.Session.0");
		this.smppConfig.setType(SmppBindType.TRANSCEIVER);
		this.smppConfig.setHost(this.hostAddress);
		this.smppConfig.setPort(this.hostPort);
		this.smppConfig.setConnectTimeout(this.timeout);
		this.smppConfig.setSystemId(this.username);
		this.smppConfig.setPassword(this.password);
		this.smppConfig.getLoggingOptions().setLogBytes(true);
		// to enable monitoring (request expiration)
		this.smppConfig.setRequestExpiryTimeout(30000);
		this.smppConfig.setWindowMonitorInterval(15000);
		this.smppConfig.setCountersEnabled(true);
		this.smppSession = this.clientBootstrap.bind(this.smppConfig, this.sessionHandler);
		this.enquireLinkResp = this.smppSession.enquireLink(new EnquireLink(), this.timeout);
		if (this.enquireLinkResp.getCommandStatus() != SmppConstants.STATUS_OK) throw new Exception("Error connection to SMPP host: " + this.enquireLinkResp.getResultMessage());
	}

	@Override
	protected void _stop() throws Exception
	{
		if (this.smppSession != null)
		{
			if (this.smppSession.isBinding() || this.smppSession.isBound()) this.smppSession.unbind(this.timeout);
			this.smppSession.destroy();
		}
		if (this.clientBootstrap != null) this.clientBootstrap.destroy();
		if (this.monitorExecutor != null) this.monitorExecutor.shutdownNow();
	}

	@Override
	protected boolean _send(OutboundMessage message) throws Exception
	{
		SubmitSm submitRequest = new SubmitSm();
		if (message.getOriginatorAddress().isVoid()) throw new Exception("Originator not set!");
		submitRequest.setSourceAddress(new Address(getTON(message.getOriginatorAddress()), getNPI(message.getOriginatorAddress()), message.getOriginatorAddress().getAddress()));
		submitRequest.setDestAddress(new Address(getTON(message.getRecipientAddress()), getNPI(message.getRecipientAddress()), message.getRecipientAddress().getAddress()));
		switch (message.getEncoding())
		{
			case Enc7:
				submitRequest.setDataCoding(SmppConstants.DATA_CODING_DEFAULT);
				submitRequest.setShortMessage(CharsetUtil.encode(message.getPayload().getText(), CharsetUtil.CHARSET_GSM));
				break;
			case EncUcs2:
				submitRequest.setDataCoding(SmppConstants.DATA_CODING_UCS2);
				submitRequest.setShortMessage(CharsetUtil.encode(message.getPayload().getText(), CharsetUtil.CHARSET_UCS_2));
				break;
			default:
				throw new RuntimeException("Unsupported Encoding!");
		}
		SubmitSmResp sumbitResponse = this.smppSession.submit(submitRequest, this.timeout);
		if (sumbitResponse.getCommandStatus() == SmppConstants.STATUS_OK)
		{
			message.setGatewayId(getGatewayId());
			message.setSentDate(new Date());
			message.getOperatorMessageIds().add(sumbitResponse.getMessageId());
			message.setSentStatus(SentStatus.Sent);
			message.setFailureCause(FailureCause.None);
		}
		else
		{
			message.setSentStatus(SentStatus.Failed);
			message.setFailureCause(FailureCause.GatewayFailure);
		}
		return (message.getSentStatus() == SentStatus.Sent);
	}

	@Override
	protected boolean _delete(InboundMessage message) throws Exception
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected DeliveryStatus _queryDeliveryStatus(String operatorMessageId) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected CreditBalance _queryCreditBalance() throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Coverage _queryCoverage(Coverage coverage) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	byte getTON(MsIsdn address)
	{
		switch (address.getType())
		{
			case International:
				return SmppConstants.TON_INTERNATIONAL;
			case National:
				return SmppConstants.TON_NATIONAL;
			case Text:
				return SmppConstants.TON_ALPHANUMERIC;
			default:
				return SmppConstants.TON_UNKNOWN;
		}
	}

	byte getNPI(MsIsdn address)
	{
		return SmppConstants.NPI_UNKNOWN;
	}

	public static class ClientSmppSessionHandler extends DefaultSmppSessionHandler
	{
		public ClientSmppSessionHandler()
		{
			super(logger);
		}

		@Override
		public void firePduRequestExpired(PduRequest pduRequest)
		{
			logger.warn("PDU request expired: {}", pduRequest);
		}

		@Override
		public PduResponse firePduRequestReceived(PduRequest pduRequest)
		{
			PduResponse response = pduRequest.createResponse();
			// do any logic here
			return response;
		}
	}
}
