# Outbound Message Routing

SMSLib may use multiple gateways for sending messages. This document described how SMSLib decides which gateway to use in order to send each message.

## Gateway characteristics

Each gateway has specific characteristics (check the [Gateway Feature Map](https://github.com/smslib/smslib/wiki/GatewayFeatureMap) for more information). These are called Capabilities in SMSLib's terms.

**Gateway characteristics cannot be set or altered by you. They are fixed and dependent of the internal SMSLib implementation or the actual bulk operator capabilities.**

Taking as an example Flash Messages, some gateways support flash messaging and some don't. When you decide to send a flash message, SMSLib will filter all defined gateways and select those who can be used for sending a flash message. These are the candidate gateways.

If no gateway can be selected as a candidate gateway, the message fails as unrouted.

## Custom Routers / Balancers

SMSLib has the concept of **Router** and **Balancer**. Notice the difference:

* A Router is like a filter which can further limit the candidate gateways. 
* A Balancer decides the order of the candidate gateways. 

SMSLib comes with two build-in Routers and two build-in Balancers - but you can create your own by entending the `org.smslib.routing.AbstractRouter` and `org.smslib.routing.AbstractBalancer` classes.

<table border='1'>
<tr>
	<td><b>Build-in Router</b></td>
	<td><b>Description</b></td>
</tr>
<tr>
	<td>DefaultRouter</td>
	<td>Does nothing...</td>
</tr>
<tr>
	<td>NumberRouter</td>
	<td>Can route messages to different gateways according to a regex applied on the destination number.</td>
</tr>
</table>

<table border='1'>
<tr>
	<td><b>Build-in Balancer</b></td>
	<td><b>Description</b></td>
</tr>
<tr>
	<td>DefaultBalancer</td>
	<td>Prioritizes all candidate gateways according to their traffic.</td>
</tr>
<tr>
	<td>PriorityBalancer</td>
	<td>Prioritizes all candidate gateways according to their priority.</td>
</tr>
</table>

## Sending a message

Once a message is routed via Capability filter and the Routers/Balancers, the result of this mechanism is a list of gateways that can be used to send the specific message.

SMSLib will then try to send the message via these gateways, starting with the first one and moving to the others, until a gateway finally manages to send the message (check the OutboundQueue description as well).

## Routing adjustments

If you wish to adjust the routing procedure, you have two options:

* Implement Routers/Balancers of your own. 
* Implement the `IRouteHook` interface. When you do this, SMSLib will call you after having done its job of Capability filtering/Routing/Balancing and before submitting the message to the queues. SMSLib will pass you the list of Gateways is has decided to use, but you can further filter the list or create (and return) a new list of Gateways according to your criteria. 
