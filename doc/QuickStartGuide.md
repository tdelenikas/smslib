# Quick Start Guide

This small guide will learn you the basics of how SMSLib work. As you will see, it is extremely easy to setup and start using SMSLib.

## Include the necessary references

### Java Environment

Make sure that you have included the necessary jar files. SMSLib consists of a single module, named `smslib-VERSION.jar`.

SMSLib is also available as a all-inclusive .jar file, named `smslib-dep-VERSION-all.jar`. This includes SMSLib's dependencies, all packaged in a single jar file.

To get the SMSLib binaries, you can:

* Download and build source code yourselves (JDK 7, Maven v2.2 required).
* Snapshot and release artifacts are frequently published at the [SMSLib private Maven repository](http://smslib.org/maven2/).

If you are using Maven, add the following dependency to your project:

```
<dependency>
   <groupId>org.smslib</groupId>
   <artifactId>smslib</artifactId>
   <version>dev-SNAPSHOT</version>
</dependency>
```

Also add the private snapshot repository:

```
<repositories>
   ...
   <repository>
      <id>smslib-snapshots</id>
      <name>SMSLib Repository</name>
      <url>http://smslib.org/maven2/snapshots/</url>
   </repository>
   ...
</repositories>
```
### .NET Framework Environment

If you work with .NET Framework, you **need** to download and install the [IKVM](http://ikvm.net) package as well.

The .DLL library follows the name: `smslib-dotnet-VERSION.dll`.

Make sure that you have included the necessary `smslib-dotnet-VERSION.dll` file in your project, as well as the `IKVM.OpenJDK.Core` library from the IKVM package.

Snapshot .DLL binaries are also published at the [SMSLib web site](http://smslib.org). Alternatively, you can build the DLL yourselves - the DLL is built via the normal `mvn package` cycle.

## The Service class

The `org.smslib.Service` is your main interface to the SMSLib library. The `org.smslib.Service` is a singleton, so use `Service.getInstance()` to reference it.

The **Service** class has many methods, and these methods are the ones that you will be using in order to use the SMSLib functionality.

**As a general directive, remember to only use methods available from the Service class. This will ensure maximum compatibility with future versions.**

Before starting any work, call

```
Service.getInstance().start();
```

The `start()` call ensures that the library is startup and ready to serve your requests.

When you want to stop the SMSLib processing, call

```
Service.getInstance().stop();
```

This will suspend all SMSLib activity, like message sending etc. You can call start() and stop() as many times as you want, although it's only logical that the life of the Service class follows the life of your application.

To stop and shutdown SMSLib, call

```
Service.getInstance().terminate();
```

The `terminate()` call ensures the proper shutdown of the SMSLib service. However, once you call terminate() you will not be able to restart the SMSLib Service again!

## The Gateway class(es)

A **Gateway** is SMSLib's term for defining a service that can send or receive sms messages. So, a gateway is almost always a gsm modem or a bulk sms operator.

You should define at least one gateway. Without any gateways, SMSLib is 'muted' and cannot do much... gateways are predefined SMSLib classes and each gateway corresponds to a bulk sms operators for who SMSLib has support. Check the [Gateway Feature Map](https://github.com/smslib/smslib/wiki/GatewayFeatureMap) for supported services and/or features.

Assuming that there is a gateway named `XYZ` which takes a username and a password as parameters, we define a Gateway like this:

```
XYZ g = new XYZ("my-username", "my-password");
```

We also need to register our gateway with Service:

```
Service.getInstance().registerGateway(g);
```

By registering a gateway, the latter is ready for use with SMSLib. We can also unregister a gateway, which instructs SMSLib to remove the specific gateway from its pool of available gateways.

In the same sense, we can define and register as many gateways as we want. SMSLib will use all of them. We could do this in order, for example, to increase performance or to route different messages via different operators.

The Service and gateway registration order is not important. You can first start the Service and register your gateways after, or the other way around. SMSLib is smart enough to do proper initialization in all cases.

### Sending a message

Sending is very easy:

```
OutboundMessage m = new OutboundMessage("301234999999", "Hello there!");
Service.getInstance().send(m);
```

We define a `OutboundMessage` object with the recipient's number (msisdn) and the actual message. Then, we pass it over to the Service, using the `send()` method. You will get back a TRUE if the message was sent successfully.

The `send()` method is a synchronous method, i.e. you will block until the message is sent (or failed). You could also use `queue()` to instruct SMSLib to send the message in the background.
