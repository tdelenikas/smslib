# Frequently Asked Questions

### What phones / modems should I use?

To use a gsm device with smslib, the former should support gsm modem functionality and should be able to connect with a serial or IP port.

* Your first choice should be a dedicated gsm modem or some 3G USB dongle.
* Some (older) phones also work nice, but I am not sure if you can find them any more...
* Your super-duper quad-processor, 5'' smart phone probably won't work at all.

You may find information about specific devices in the [SMSLib Discussion Group](https://groups.google.com/d/forum/smslib).

### How are msisdn numbers represented?

You generally need to use the '+' sign to define an international number. Numbers without the initial '+' are supposed to be national - something that may or may not work.

So

```
MsIsdn recipient = new MsIsdn("306974000000");
```

is defined as a national number, but most of the times it works as the network recognizes the international number prefix. For more fine grained control, or if you face issued with  your provider, you can use the detailed syntax like this:


```
MsIsdn recipient = new MsIsdn("306974000000", Type.International);
```

or

```
MsIsdn recipient = new MsIsdn("6974000000", Type.National);
```

### How fast can SMSLib send messages?

If you are using GSM modems, SMSLib can send with a rate of about 6 messages per minute. This is a GSM network limit. This rate is actually lower, as you are advised not to send messages continuously without pausing and letting the modem take a breath. GPRS sending is not currently supported.

If you are using one of the supported bulk operators, the dispatch speed is much higher.

### Can SMSLib send and receive big (multipart) messages?

Yes.

### What encodings does SMSLib support?

SMSLib supports 7bit (GSM default alphabet), 8bit (aka binary) and Unicode (UCS2) encoding.

### Does SMSLib support port addressing?

Yes.

### Does SMSLib support Status/Delivery report processing?

Yes.

### Can SMSLib be used for ringtones/logos and other EMS/MMS messaging?

No.

### Does SMSLib support SMPP?

Not yet.
