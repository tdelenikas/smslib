# Gateway Feature Map

Here is the Gateway Support / Feature Map, the list of gateways currently supported by SMSLib, as well as the supported features for each Gateway. Also check out what is **not supported**, at the section near the end of this document.

**Important Notice:** This is **not a list comparing bulk sms operators** - the list is about the features supported by SMSLib. If you find that feature X missing, it may as well be that SMSLib does not support it, although the respective operator does.

## Gateway classes

<table border='1'>
<tr>
	<td><b>Gateway</b></td>
	<td><b>SMSLib class</b></td>
	<td><b>Description</b></td>
<tr>
<tr>
	<td>Mock gateway</td>
	<td>org.smslib.gateway.MockGateway</td>
	<td>Simple mock gateway (used for testing, does not send or receive anything for real!</td>
</tr>
<tr>
	<td>GSM Modem</td>
	<td>org.smslib.modem.Modem</td>
	<td>Serial or IP GSM modem gateway</td></tr>
<tr>
	<td>BULKSMS</td>
	<td>org.smslib.gateway.http.bulksms.BulkSmsInternational</td>
	<td><a href="http://www.bulksms.com/int/">BULKSMS International</a></td>
</tr>
<tr>
	<td> </td>
	<td>org.smslib.gateway.http.bulksms.BulkSmsDE</td>
	<td><a href="http://bulksms.de/">BULKSMS DE</a></td>
</tr>
<tr>
	<td> </td>
	<td>org.smslib.gateway.http.bulksms.BulkSmsES</td>
	<td><a href="http://bulksms.com.es/">BULKSMS ES</a></td>
</tr>
<tr>
	<td> </td>
	<td>org.smslib.gateway.http.bulksms.BulkSmsSA</td>
	<td><a href="http://bulksms.2way.co.za/">BULKSMS SA</a></td>
</tr>
<tr>
	<td> </td>
	<td>org.smslib.gateway.http.bulksms.BulkSmsUK</td>
	<td><a href="http://www.bulksms.co.uk/">BULKSMS UK</a></td>
</tr>
<tr>
	<td> </td>
	<td>org.smslib.gateway.http.bulksms.BulkSmsUS</td>
	<td><a href="http://usa.bulksms.com/">BULKSMS US</a></td>
</tr>
<tr>
	<td>Nexmo</td>
	<td>org.smslib.gateway.http.nexmo.Nexmo</td>
	<td><a href="http://www.nexmo.com/">Nexmo</a></td>
</tr>
<tr>
	<td>TXTImpact</td>
	<td>org.smslib.gateway.http.txtimpact.TXTImpact</td>
	<td><a href="http://www.txtimpact.com/">TXTImpact</a></td>
</tr>
<tr>
	<td>Clickatell</td>
	<td>org.smslib.gateway.http.clickatell.Clickatell</td>
	<td><a href="http://www.clickatell.com/">Clickatell</a></td>
</tr>
<tr>
	<td>TextMagic</td>
	<td>org.smslib.gateway.http.textmagic.TextMagic</td>
	<td><a href="http://www.textmagic.com/">TextMagic</a></td>
</tr>
</table>

## Features

<table border='1'>
<tr>
	<td><b>Features / Gateways</b></td>
	<td>Modem<sup><b>A</b></sup></td>
	<td>BULKSMS<sup><b>B</b></sup></td>
	<td>Nexmo</td>
	<td>TXTImpact</td>
	<td>Clickatell</td>
	<td>TextMagic</td>
</tr>
<tr>
	<td>Inbound</td>
	<td>Y</td>
	<td>N</td>
	<td>N</td>
	<td>N</td>
	<td>N</td>
	<td>N</td>
</tr>
<tr>
	<td>Outbound</td>
	<td>Y</td>
	<td>Y</td>
	<td>Y</td>
	<td>Y</td>
	<td>Y</td>
	<td>Y</td>
</tr>
<tr>
	<td>Outbound>160ch</td>
	<td>Y</td>
	<td>Y</td>
	<td>Y</td>
	<td>N</td>
	<td>Y</td>
	<td>Y</td>
</tr>
<tr>
	<td>Outbound Binary</td>
	<td>Y</td>
	<td>N</td>
	<td>N</td>
	<td>N</td>
	<td>N</td>
	<td>N</td>
</tr>
<tr>
	<td>Outbound WAP</td>
	<td>Y</td>
	<td>N</td>
	<td>N</td>
	<td>N</td>
	<td>N</td>
	<td>N</td>
</tr>
<tr>
	<td>Outbound Unicode</td>
	<td>Y</td>
	<td>N</td>
	<td>Y</td>
	<td>N</td>
	<td>Y</td>
	<td>Y</td>
</tr>
<tr>
	<td>Outbound with port info</td>
	<td>Y</td>
	<td>N</td>
	<td>N</td>
	<td>N</td>
	<td>N</td>
	<td>N</td>
</tr>
<tr>
	<td>Outbound flash</td>
	<td>Y</td>
	<td>Y</td>
	<td>N</td>
	<td>N</td>
	<td>Y</td>
	<td>N</td>
</tr>
<tr>
	<td>HTTPS</td>
	<td>N/A</td>
	<td>Y</td>
	<td>Y</td>
	<td>Y</td>
	<td>Y</td>
	<td>Y</td>
</tr>
<tr>
	<td>Custom Sender ID<sup><b>C</b></sup></td>
	<td>N/A</td>
	<td>Y</td>
	<td>Y</td>
	<td>N</td>
	<td>Y</td>
	<td>Y</td>
</tr>
<tr>
	<td>Query Delivery</td>
	<td>Y</td>
	<td>Y</td>
	<td>N</td>
	<td>N</td>
	<td>Y</td>
	<td>Y</td>
</tr>
<tr>
	<td>Query Coverage</td>
	<td>N</td>
	<td>N</td>
	<td>N</td>
	<td>N</td>
	<td>~</td>
	<td>~</td>
</tr>
<tr>
	<td>Query Balance</td>
	<td>N</td>
	<td>Y</td>
	<td>Y</td>
	<td>Y</td>
	<td>Y</td>
	<td>Y</td>
</tr>
</table>

<b><sup>A</sup></b> Both serial and ip modems are supported.

<b><sup>B</sup></b> All BULKSMS regional gateways are supported.

<b><sup>C</sup></b> Feature may require prior registration/approval from the operator.

<b><sup>~</sup></b> Planned

## Unsupported functions

The current SMSLib v4 version does **not** support the following:

* USSD
* SMPP
* EMS/MMS messaging
