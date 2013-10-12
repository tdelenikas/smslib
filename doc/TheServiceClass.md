# The Service Class

The `org.smslib.Service` is your main interface to the SMSLib library. The `org.smslib.Service` is a singleton, so use `Service.getInstance()` to reference it.

**As a general directive, remember to only use methods available from the Service class. This will ensure maximum compatibility with future versions.**

## Startup / Shutdown

### start()

The first thing you should do is call `start()`. This method initializes the SMSLib library, starts background threads, etc and set it at a ready state to serve your requests.

### stop()

The `stop()` stops the SMSLib processing. After this call, SMSLib will not serve any requests (except `registerGateway()` & `unregisterGateway()`). Message processing will also be stopped after you call `stop()`.

Notes:

 * You may start and stop the SMSLib Service as many times as you want.
 * Callbacks continue to work when the Service is stopped.

### terminate()

The `terminate()` shuts down the Service. For the .NET Framework, it also destroys the background threads which remain active - not calling `terminate()` in a .NET Framework application may leave the application in a blocked state...

Note that once you terminate the SMSLib Service, there is no way to bring it up again!

## Gateway related operations

### registerGateway()

The `registerGateway()` method registers a gateway with SMSLib. Once registered, a gateway is ready to be used.

### unregisterGateway()

The `unregisterGateway()` method unregisters a gateway, i.e. removes it from the pool of the available SMSLib gateways and will not use it again.

You may register / unregister gateways any time you want (i.e. whether the Service is started or stopped).

## Sending messages

### send()

The `send()` method sends a message. It's a synchronous method - it blocks you until the message is sent or failed. Routing rules are applied.

The `send()` method returns TRUE if the message was sent successfully. Extra information is stored in the OutboundMessage member fields.

### queue()

The `queue()` method queues a message for dispatch. It's an asynchronous method - it returns immediately. SMSLib sends queued messages in the background, and informs you about their fate via the MessageSentCallback callback method described below.

## Callback methods

Callbacks are function calls triggered by SMSLib and let you know that specific events have happened. You may choose to 'listen' to these events (and get notified) or ignore them. If you wish to subscribe to these events, you should implement the necessary interfaces and register them as listeners.

Once an event is generated, your registered handlers will be invoked. The parameter passed to you contains information about the exact event that happened.

Once you receive an event (i.e. you callback method is called) be sure to return TRUE once you process the event. Otherwise, SMSLib will re-queue the event and call you again and again, until it receives a TRUE from your handler.

The SMSLib library is asynchronous as far as the callbacks are concerned. The exact time of the event is not the time your listener is called! That's why the event parameters passed to your listeners contain the actual timestamp of the event.

<table border='1'>
<tr>
	<td><b>Callback</b></td>
	<td><b>Description</b></td>
	<td><b>Interface</b></td>
</tr>
<tr>
	<td>ServiceStatusCallback</td>
	<td>Fires when the Service changes its status, for example was stopped and got started.</td>
	<td>IServiceStatusCallback</td>
</tr>
<tr>
	<td>GatewayStatusCallback</td>
	<td>Fires when a Gateway changes its status, for example was stopped and got started.</td>
	<td>IGatewayStatusCallback</td>
</tr>
<tr>
	<td>InboundMessageCallback</td>
	<td>Fires when an Inbound Message is received.</td>
	<td>IInboundMessageCallback</td>
</tr>
<tr>
	<td>DeliveryReportCallback</td>
	<td>Fires when a Delivery Report is received.</td>
	<td>IDeliveryReportCallback</td>
</tr>
<tr>
	<td>InboundCallCallback</td>
	<td>Fires when an Inbound Call is received.</td>
	<td>IInboundCallCallback</td>
</tr>
<tr>
	<td>MessageSentCallback</td>
	<td>Fires when an Outbound Message was sent or has failed to sent.</td>
	<td>IMessageSentCallback</td>
</tr>
<tr>
	<td>DequeueMessageCallback</td>
	<td>When the Service is stopped, queued messages will be 'drained' back to you via this callback.</td>
	<td>IDequeueMessageCallback</td>
</tr>
<tr>
	<td>QueueThresholdCallback</td>
	<td>When SMSLib queue load drops between the specified limits (check Settings.queueCallbackLowThreshold and Service.queueCallbackHighThreshold) , a callback is made to you.</td>
	<td>IQueueThresholdCallback</td>
</tr>
</table>

## Hooks

Hooks are similar to callbacks, but:

 * They are synchronous and you must answer something to them - and answer in a timely fashion as well!
 * Your answer may modify the SMSLib behaviour.

Similar to the callbacks, you may choose to 'listen' to hooks or live without them.

<table border='1'>
<tr>
	<td><b>Hook</b></td>
	<td><b>Description</b></td>
	<td><b>Interface</b></td>
</tr>
<tr>
	<td>PreSendHook</td>
	<td>Fires just before a message is send out. It's your last chance to cancel that message. You are expected to return TRUE and allow the message to be sent, or return FALSE and cancel the message.</td>
	<td>IPreSendHook</td>
</tr>
<tr>
	<td>PreQueueHook</td>
	<td>Fires when you queue a message. It's your last chance to cancel that message. You are expected to return TRUE and allow the message to be queued, or return FALSE and cancel the message.</td>
	<td>IPreQueueHook</td>
</tr>
<tr>
	<td>RouteHook</td>
	<td>Fires after SMSLib finished the routing of the message. It passes you the final selection of gateways (as the result of the SMSLib routing logic) and you have the ability to modify the candidate gateways. Delete one, add one, or return a completely new selection of gateways. As a side effect, if you return an empty collection from this hook, the message is dropped as un-routable.</td>
	<td>IRouteHook</td>
</tr>
</table>
