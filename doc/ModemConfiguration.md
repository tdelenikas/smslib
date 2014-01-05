# Modem Configuration

SMSLib has some settings that affect modem operation. For the majority of cases, the default settings should be ok. If you find that this is not the case, you may need to alter SMSLib behaviour either globally or for specific modems / brands.

## Configuration file

The configuration file is called `modem.properties`, located in folder `smslib/src/main/resources`. It's a plain text file.

Settings are hierarchically defined. Settings starting with *default* are the (guess what...) default values. You can override the settings, by redefining the setting by the appropriate key starting with the *modem signature id*. The look-up order start with the **manufacturer/model** specific settings, falls back to **manufacturer** specific settings and finally to **default** settings.

For example:

<table border='1'>
<tr>
	<td><code>default.memory_locations=SM</code></td>
	<td>Sets the global default for modem storage locations to "SM". That is, no matter which modem you'll use, only "SM" will be read.</td>
</tr>
<tr>
	<td><code>huawei.memory_locations=SM</code></td>
	<td>Sets the storage locations to "SM" for modems having a <b>manufacturer</b> signature "huawei". This will affect every HUAWEI modem you will use, independent of model.</td>
</tr>
<tr>
	<td><code>huawei_xyz.memory_locations=SMSR</code></td>
	<td>Sets the storage locations to "SM" for modems having a <b>manufacturer/model</b> signature "huawei_xyz". This will affect every HUAWEI modem, model "xyz" you will use.</td>
</tr>
</table>

**How to locate your modem's Signature ID?**

Start by modifying the source file `/smslib-gsm/src/test/java/org/smslib/Test_SerialModem.java`. Set *your* comm port and baud rate.

Run:

```
mvn clean test -Dtest=Test_SerialModem
```

An INFO level line will appear on screen - search and locate it, it's similar to the following:

```
99918  2012-06-13 15:43:47,889 [main] INFO  org.smslib.gateway.modem.Modem  - Gateway: modem1 (GSM Modem) [COM3:19200]: MAN:HOJY, MOD:WYLESS_600, SN:353036025712340, IMSI:202052973306725, SW:HW:1.0.0.2 SW:1.2.0.15, RSSI:-63dBm, SL:SM, SIG: hojy_wyless_600 / hojy
```

This line displays information about your modem, like Manufacturer strings, Signal Level, etc.

Notice the last part:

```
SIG: hojy_wyless_600 / hojy
```

This is the signature of your modem, as defined by SMSLib. It contains two parts: `hojy_wyless_600` and `hojy`, delimited by a forward slash (/).

If you want to use a **manufacter/model** signature, use the first part.
If you want to use a **manufacter** signature, use the second part.

## Available configuration options
