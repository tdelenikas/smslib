# The Outbound Queue(s)

SMSLib has a background sending feature which is based on Queues. It's important to understand how this works in order to take advantage of it.

## Design

Depending on your setup, SMSLib may define multiple queues:

* By default, a single master queue is available, which holds all OutboundMessage objects which are queued for dispatch via the `Service.queue()` method.
* Each Gateway also manages its own gateway queue.
* For each queue, a background managing thread is deployed. For the master queue, this thread is an instance of the ServiceMessageDispatcher class. For the gateway queue(s), this thread is an instance of the GatewayMessageDispatcher class.

So, assuming that you work with 5 gateways, your SMSLib application spawns:

* The master queue, along with its own managing thread.
* 5 gateways queues with 5 respective managing threads.

All queues are non-persistent, in-memory queues. While there is no fixed capacity limit, queues take available heap/RAM space.

## Queue Management

When a message is queued for delivery, the following operations happen:

* You call `Service.queue()` in order to queue your message.
* Your message is placed on the master queue.
* The ServiceMessageDispatcher wakes up, and applies routing to your message. As described in the Routing description, the result of routing is a list of gateways which can send the specific message. If there is no list of gateways available, the ServiceMessageDispatcher may choose either to drop the message or keep it and reprocess it later (this behaviour is described below). Ideally, there will be a list of gateways, so the ServiceMessageDispatcher moves the message from the master queue to the queue of the first gateway of the above mentioned list. During this step, the ServiceMessageDispatcher saves the gateway lists in the OutboundMessage.
* The GatewayMessageDispatcher of the specific gateway which received the message wakes up and tries to send the message. If it fails to send the message:
 * It updates the OutboundMessage routing table and removes itself from it.
 * Forwards the OutboundMessage to the queue of the next gateway in the routing table (if any), or marks it as failed.

## Frequently asked questions

Below are some real world situations and how SMSLib responds to these cases:

### What if a gateway goes down...

During SMSLib operation, a gateway can go down. For example, imagine the situation where you unregister a gateway during operation. If the specific gateway's queue has unsent messages, SMSLib will move them back in the master queue in order to allow them to get re-routed via other gateways. No messages are lost!

### What if I queue messages without having any gateways...

If you queue messages without having defined any gateways or without having started the Service, there are two options:

SMSLib will fail these messages as unroutable (default). SMSLib will keep them and try to send them later. The behaviour is configurable. By default, messages will be rejected with the "No Route" failure cause. If you want to instruct SMSLib to keep them queued, set `Settings.keepOutboundMessagesInQueueIfNoGatewaysStarted = true;` from inside your application.

### What happens when the Service is stopped?

If you call `Service.stop()` (for example to temporarily stop SMSLib) and then `Service.start()`, nothing really happens. The queues hold their contents. Dispatchers and gateways shutdown and get restarted. Messages will be re-routed though... During Service restarts, no messages are lost but a re-route overhead will occure.

When you call `Service.terminate()`, something different happens:

* If there are pending outbound messages in the queue(s) and you've subscribed to the DequeueMessageCallback callback, SMSLib will push all pending outbound messages back to you, via the callback.
* If the queues have pending outbound messages and you haven't subscribed to the DequeueMessageCallback callback, the pending messages will be lost!
* If the queues are empty, no harm done.
