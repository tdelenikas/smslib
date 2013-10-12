# General Configuration

SMSLib has some configuration parameters which allow you to alter its behavious without modifying code. All configuration parameters are stored in the class `Settings` as static members. To change them, use the following code:

```
Settings.httpServerACLStatus = "127.0.0.1/32";
```

The majority of configuration parameters can be altered at runtime and have immediate effect. Some parameters are also accessible via `-Dxxxx=yyyy` runtime definitions - those which cannot be modified at runtime. For example, the port to which the internal HTTP server listens to.

### Parameters

<table border='1'>
<tr>
	<td><b>Configuration Parameter</b></td>
	<td><b>Default Value</b></td>
	<td><b>Description</b></td>
	<td><b>Runtime option</b></td>
<tr>
<tr>
	<td>httpServerPort</td>
	<td>8001</td>
	<td>Listening port of the embedded HTTP server.</td>
	<td>-Dsmslib.httpserver.port=8888</td>
</tr>
<tr>
	<td>keepOutboundMessagesInQueue</td>
	<td>FALSE</td>
	<td>Determines whether SMSLib will keep queued messages in queue when no gateways are defined/started (TRUE) or if SMSLib will fail those queued messages as UNROUTABLE.</td>
	<td>N/A</td>
</tr>
<tr>
	<td>deleteMessagesAfterCallback</td>
	<td>FALSE</td>
	<td>If TRUE, SMSLib will delete inbound messages once the callback methods have been called. If FALSE, nothing is automatically deleted - you need to delete messages yourself in order to avoid receiving the same messages again and again.</td>
	<td>N/A</td>
</tr>
<tr>
	<td>hoursToRetainOrphanedMessageParts</td>
	<td>72 (hours)</td>
	<td>The period (in hours) for which SMSLib will retain non-completed messages, waiting for the rest of the parts to be received in order to reconstruct the entire message. If this retain period lapses, SMSLib will automatically delete these "orphaned" parts. </td>
	<td>N/A</td>
</tr>
</table>
