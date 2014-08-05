# General Configuration

SMSLib has some configuration parameters which allow you to alter its behaviour without modifying code. All configuration parameters are stored in the class `Settings` as static members.

Some parameters are also accessible via `-Dxxxx=yyyy` runtime definitions.

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
	<td>-Dsmslib.keepoutboundmessagesinqueue=false</td>
</tr>
<tr>
	<td>deleteMessagesAfterCallback</td>
	<td>FALSE</td>
	<td>If TRUE, SMSLib will delete inbound messages once the callback methods have been called. If FALSE, nothing is automatically deleted - you need to delete messages yourself in order to avoid receiving the same messages again and again.</td>
	<td>-Dsmslib.deletemessagesaftercallback=false</td>
</tr>
<tr>
	<td>hoursToRetainOrphanedMessageParts</td>
	<td>72 (hours)</td>
	<td>The period (in hours) for which SMSLib will retain non-completed messages, waiting for the rest of the parts to be received in order to reconstruct the entire message. If this retain period lapses, SMSLib will automatically delete these "orphaned" parts. </td>
	<td>-Dsmslib.hourstoretainorphanedmessageparts=72</td>
</tr>
</table>

### Other Configuration

#### HTTP Server

SMSLib runs an embedded HTTP server. By visiting `http://localhost:8001/status`, you will see the SMSLib status page with some useful information about the Gateways running, basic messaging statistics, etc

The URLs supported as well as the ACLs per URL are configurable.

Remember that, if you have defined **no** ACL, access is denied! So you should define the ACLs you want.

For example, with the default configuration the status page (`http://localhost:8001/status`) is inaccessible. You should add a call like the following:

```
Service.getInstance().registerHttpRequestACL("/status", "0.0.0.0/0");
```

to make the `/status` visible.

**Note**: When using SMSServer, the `/status` is enabled for everybody.
