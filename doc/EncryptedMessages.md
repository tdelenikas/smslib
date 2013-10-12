#Encrypted Messages

SMSLib offers you the functionality to send and receive encrypted messages. This is useful for SMSLib-to-SMSLib messaging.

Only binary messages can be encrypted.

If both parties are using SMSLib (i.e. both the originator and the recipient), SMSLib provides all the necessary functionality to automatically encrypt/decrypt the messages with minimal difficulty. Even if one party is using SMSLib, you can easily decrypt messages yourselves at the other end, once you get the idea of how this is done.

## Encryption

SMSLib currently uses the AES strong key (128bit) encryption algorithm, which is available since JDK 5. It's strong enough and spares the core library from external dependencies. Other algorithms or stronger cipher implementation are easy to be implemented as well. The current SMSLib implementation is for symmetric encryption algorithms - the next step will be to implement an asymmetric algorithm as well in order to have it as reference.

## The `org.smslib.crypto` package

The `org.smslib.crypto` contains the following classes:
<table border='1'>
<tr>
	<td>AKey</td>
	<td>The base, abstract class for the definition of an encryption key. Its pretty much empty right now.</td>
</tr>
<tr>
	<td>ASymmetricKey</td>
	<td>The base class for the implementation of keys for symmetric algorithms. Contains helper functions for the maintenance and generation of keys and for the encoding / decoding of data blocks.</td>
</tr>
<tr>
	<td>AESKey</td>
	<td>The implementation class for the AES algorithm.</td>
</tr>
<tr>
	<td>KeyManager</td>
	<td>In an attempt to automate the encryption/decryption as much as possible, SMSLib contains a KeyManager implementation. The role of the KeyManager class is to hold pairs of numbers (i.e. originators or recipients) and encryption keys. Once this is setup, SMSLib will automatically encrypt (or decrypt) messages send to (or received from) numbers which are defined in the KeyManager.</td>
</tr>
</table>

## Message classes

SMSLib has two new classes implemented: The `InboundEncryptedMessage` and the `OutboundEncryptedMessage`. These classes encapsulate the details of the encrypted messages.

## How everything wraps up

Here are the details for the implementation of message encryption.

### Setup the KeyManager

Before attempting to send or receive messages, you need to setup the details in the KeyManager. The KeyManager holds encryption keys per source or destination. So, assuming that you need to send an encrypted message to a certain number, you should declare this as follows:

```
Service.getInstance().getKeyManager().registerKey(
	"306900000000",
	new AESKey(new SecretKeySpec("0011223344556677".getBytes(), "AES")));
```

This command makes SMSLib remember that when sending an encrypted message to "306900000000", it should use the AES algorithm with the key "0011223344556677". You should similarly declare all originator / recipient numbers and their keys.

### Sending an encrypted message

Once the key is set, you are ready to send the message. Remember to use the new OutboundEncryptedMessage class!

```
OutboundEncryptedMessage msg = new OutboundEncryptedMessage(
	"306900000000",
	"Hello (encrypted) from SMSLib!".getBytes());
```

That's all!

###Receiving encrypted messages

Once again, before reception of messages you should set up the KeyManager in order for SMSLib to know and decrypt received messages correctly.

The reception of messages works like before. In the case where a message is received from a recipient which is declared in the KeyManager, SMSLib will push a message of InboundEncryptedMessage class.

## Frequently asked questions

### I want to implement another symmetric algorithm.

Extend the ASymmetricKey class with the necessary code. Have a look at the AESKey for reference.

### What if you forget the KeyManager definitions?

Outbound messages will throw exceptions upon creation of the `OutboundEncryptedMessage` object. Inbound messages will be received as binaries with "ciphered", unreadable text.

### What if the key in KeyManager is wrong?

Outbound messages will be sent, however the wrong key will prevent the recipient from decoding your data. Inbound messages will throw exceptions upon receipt, as the crypto algorithm will fail to decrypt the content.