# Using GSM Modems

With SMSLib, you have the option to work with GSM modems (or GSM phones which have GSM modem functionality) as gateways. GSM Modems can be used for inbound or outbound messaging. GSM modems do not support all SMSLib features - check [Gateway Feature Map](https://github.com/smslib/smslib/wiki/GatewayFeatureMap) for more information about what is supported in the current SMSLib release.

As with other gateways, SMSLib allows you to setup more than one modems and will manage all of them.

## Set up a modem

Modems come in two flavors:

* Serial modems
* IP modems

Each type of modem requires a different setup, so please read carefully the following instructions on how to setup your modem(s)

### Serial modems

Serial modems are those modems which connect to your PC/Server via a serial port. By serial port, we mean all sorts of connections that appear as a physical or virtual serial port on your host. You may use a classic serial interface, or a USB interface which appears as a virtual com port on your machine.

In order to use a serial modem, you must manually install the java communication libraries. You have several options about which comm library to use - check the following map:

<table border="1">
<tr><td><b>Operating system</b></td><td><b>JavaComm library</b></td></tr>
<tr><td>x86 systems</td><td><a href="http://smslib.org/download/">JavaComm v2</a> for Windows, or <a href="http://rxtx.qbang.org/pub/rxtx/rxtx-2.1-7-bins-r2.zip">RxTx v2.1.7</a>.</td></tr>
<tr><td>x64 systems</td><td><a href="http://mfizz.com/oss/rxtx-for-java">RxTx 64bit builds.</a></td></tr>
<tr><td>x64 systems</td><td>Alternatively, install the 32bit JDK and follow the x86 instructions above.</td></tr>
<tr><td>Linux/Unix or other O/S</td><td><a href="http://rxtx.qbang.org/">RxTx</a> (probably) has a build for you. Or check out the <a href="http://www.oracle.com/technetwork/java/index-jsp-141752.html">Java Comm v3</a> directly from Oracle.</td></tr>
</table>

Follow the installation instructions of the comm package you choose **very carefully!** Most (if not all) modem connectivity problems are due to incorrent java comm installation.

For the JavaComm v2/Win32 package, make the following copies:

* File `comm.jar` should go under `JDKDIR/jre/lib/ext/`
* File `javax.comm.properties` should go under `JDKDIR/jre/lib/`
* File `win32com.dll` should go under `JDKDIR/jre/bin/`

For the RxTx package, you usually have to:

* File `RXTXcomm.jar` should go under `JDKDIR/jre/lib/ext/`
* The necessary library file (e.g.. for Linux 32bit, the `librxtxSerial.so`) should go under JDKDIR/jre/bin/

If you have a separate JRE directory, do the same copies for the JREDIR directory!

If your modem has USB connectivity, it's possible that a special driver is also required and must be installed in order for your modem to acquire a virtual com port. These are usually called modem drivers. Check with your manufacturer's web site or manuals for further information.

### Serial modems via network

If you don't want to mess with serial ports, Java comm etc you can connect to your serial modems via network, if you install one of the many serial-to-ip emulation applications. There are many such applications for all operating systems, free and commercial ones. SMSLib also has it's own, named [Comm2IP](https://github.com/smslib/comm2ip) (Win/.NET). For Linux, for example, you could use [ser2net](http://sourceforge.net/projects/ser2net/). With such an interface, your serial modems may even be on a separate machine that the one you use for building your SMSLib powered applications and you can connect to them as if they were IP modems. No need for RxTx, Javacomm or other external dll/so files!

### IP modems

IP modems do not require anything special or any 3rd party libraries. Just make sure of the obvious: have network access to your modem's listening port.

### Microsoft .NET Framework

The SMSLib for Microsoft .NET Framework sees all modems as IP Modems. If you have a serial modem, you can use the [Comm2IP](https://github.com/smslib/comm2ip) in order to map your serial port to an IP endpoint. From then on, use your modem as an IP modem.

## Modem scan & auto-detection

Assuming that you have succesfully installed the COM library of your choice, you can try and check if SMSLib recognizes your modem. Just run the plain jar:

```
java -jar smslib-dep-dev-SNAPSHOT-all.jar
```

You will see information *like* the following:

```
log4j:WARN No appenders could be found for logger (org.smslib.threading.CallbackManager).
log4j:WARN Please initialize the log4j system properly.
log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html#noconfig for more info.
SMSLib - A universal API for sms messaging
Copyright (c) 2002-2014, smslib.org
This software is distributed under the terms of the
Apache v2.0 License (http://www.apache.org/licenses/LICENSE-2.0.html).
SMSLib Version: dev-SNAPSHOT
OS Version: Windows 7 / x86 / 6.1
JAVA Version: 1.8.0_11
JAVA Runtime Version: 1.8.0_11-b12
JAVA Vendor: Oracle Corporation
JAVA Class Path: smslib-dep-dev-SNAPSHOT-all.jar

Running port autodetection / diagnostics...

====== Found port: COM3
>> Trying at   9600...  Getting Info... Found: AT+CGMMERROR
>> Trying at  14400...No device... (Unsupported baud rate)
>> Trying at  19200...  Getting Info... Found: AT+CGMMERROR
>> Trying at  28800...No device... (Unsupported baud rate)
>> Trying at  33600...No device... (Unsupported baud rate)
>> Trying at  38400...  Getting Info... Found: AT+CGMMERROR
>> Trying at  56000...No device... (Unsupported baud rate)
>> Trying at  57600...  Getting Info... Found: AT+CGMMERROR
>> Trying at 115200...  Getting Info... Found: AT+CGMMERROR
====== Found port: COM1
>> Trying at   9600...  Getting Info... Found: AT+CGMMNokia 6310i
>> Trying at  14400...No device... (Unsupported baud rate)
>> Trying at  19200...  Getting Info... Found: AT+CGMMNokia 6310i
>> Trying at  28800...No device... (Unsupported baud rate)
>> Trying at  33600...No device... (Unsupported baud rate)
>> Trying at  38400...  Getting Info... Found: AT+CGMMNokia 6310i
>> Trying at  56000...No device... (Unsupported baud rate)
>> Trying at  57600...  Getting Info... Found: AT+CGMMNokia 6310i
>> Trying at 115200...  Getting Info... Found: AT+CGMMNokia 6310i

Test complete.
```

Here, my old-fashioned Nokia 6310i is discovered on port COM1. This function actually iterates all found ports and checks if something is attached and responding.

## Modem gateway initialization

A Serial Modem gateway is initialized like this:

```
Modem gateway = new Modem("modem1", "COM4", "19200", "0000", "0000", "306942190000");
```

The required parameters are:

* A gateway name.
* The com port, something like "COMx" for Windows or "/etc/ttyS20" for Linux.
* The baud rate.
* SIM Pin
* SIM Pin2
* The Message Service Center number of your network / provider. If you don't provide one, your modem will try to use it's default SC number - this may or may not work properly.

An IP Modem gateway is initialized like this:

```
Modem gateway = new Modem("modem2", "192.168.1.100", "5000", "0000", "0000", "306942190000");
```

The required parameters are:

* A gateway name.
* The listening ip address of your modem.
* The listening port of your modem.
* SIM Pin
* SIM Pin2
* The Message Service Center number of your network.

## Troubleshooting

### Linux and undetectable ports

If you using USB/Bluetooth/IrDA connections on Linux, RxTx may not recognize your serial device / port. You may get a NoSuchPortException.

Assuming that your port is named `/dev/xyz-port` , you can:

* Either set the `gnu.io.rxtx.SerialPorts` system property with your device name.
* Create a symbolic link of your `/dev/xyz-port` to something resembling a standard serial port, like `/dev/ttyS20`, and use this one instead.

### Linux and unresolved references

When running under Linux and RxTx, you may encounter some unresolved errors during execution. These errors mean that your Java runtime cannot locate the necessary RxTx library (.so).

To resolve this, run your application with the `-Djava.library.path=/dir-path/to/.so/file` in order to instruct java to search to the specific directory where you have placed the RxTx so files.

### Framing errors

If you are getting one or two framing errors during the initial connection (and this happens on a random basis) its probably nothing.

If you are consistently getting framing errors, probably you are trying to connect with a baud rate that is unsupported by your modem. Some modems require a fixed baud rate to operate. Either consult your manual or try with different baud speeds.

### Identify the port a modem is attached to

If you run the SMSLib jar file alone, it will run an auto-detect function which will iterate all "visible" comm ports of your system and will try to communicate with the devices attached to them.

Run

```
java -jar smslib-dep-dev-SNAPSHOT-all.jar
```

and observe the output.

## Notes about modem usage

### Inbound Messages

When you use a modem, SMSLib keeps account of each inbound message it has forwarded via the listeners, and does not forward the same message over and over again.

This does not mean that the message is lost! The message is still stored inside your phone or modem memory. If you restart SMSLib, the same messages will be forwarded to you. The messages are removed only if you delete them.

It's important to know and keep this behaviour in mind when developing applications using SMSLib. Since the modem's memory is finite, it's best to delete all messages that have been already processed. Note that if your phone's/modem's memory fills up, you will not be able to receive any more messages, and you run the risk that extra inbound messages will be dropped altogether (i.e. pass their validity period without being able to be delivered, due to full memory).
