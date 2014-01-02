# Introduction

Welcome to the **SMSLib v4** release.

## What is SMSLib?

SMSLib is a SMS messaging library.

The purpose of this library is to provide a universal texting API, which can be used for sending and receiving messages via GSM modems or bulk sms operators.

Together with the universal API, SMSLib provides a messaging infrastructure with features like multiple gateways, routing, callbacks, hooks, etc. By unifying operators' requests and responses and by providing a rich set of features, SMSLib aims to be your favorite library for sms messaging.

SMSLib is available for Java and .NET Framework platforms. SMSLib is a pure Java library with minimal dependencies. The .NET Framework assembly is created with the help of the mighty [IKVM Tools](http://ikvm.net/).

SMSLib is licensed by the terms of the [Apache v2 License](http://www.apache.org/licenses/LICENSE-2.0.html).

## Compatibility issues

Since SMSLib v4 is redesigned to a great extent, **it's incompatible with SMSLib v3**. Most of the concepts though are the same, so the transition would not be very difficult.

## Features

### Multiple gateways

A gateway is a service that can send messages - it's the representation of a modem or bulk sms operator. SMSLib can handle many gateways simultaneously.

### Routing

You can define rules which SMSLib will follow in order to select which gateway to use for the message dispatch.

### Callbacks

SMSLib can notify you on certain events, like a gateway failure or a message sent success/failure.

### Hooks

SMSLib provides hooks, which are special callback methods which allow you to alter its behaviour.

### Asynchronous message reception

No more `readMessages()`! Just register your listener and SMSLib will call you upon message receipt.

### Synchronous and asynchronous sending operation

You may synchronously send a message (and block until the operation completes) or you may as well load a few thousand messages and let SMSLib do the work in the background. Don't worry, SMSLib will inform you about the fate of each message!

### Capabilities

Each gateway has specific characteristics about what can do and what cannot do. One gateway may be able to send flash messages and another gateway is not. SMSLib uses this information to apply correct gateway routing. Check the [Gateway Feature Map](https://github.com/smslib/smslib/wiki/GatewayFeatureMap) for more information.
