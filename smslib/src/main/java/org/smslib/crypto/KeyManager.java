
package org.smslib.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.smslib.message.MsIsdn;

public class KeyManager
{
	HashMap<MsIsdn, AbstractKey> keys;

	public KeyManager()
	{
		this.keys = new HashMap<>();
	}

	public void registerKey(MsIsdn msisdn, AbstractKey key)
	{
		this.keys.put(msisdn, key);
	}

	public void registerKey(String msisdn, AbstractKey key)
	{
		registerKey(new MsIsdn(msisdn), key);
	}

	public void unregisterKey(MsIsdn msisdn)
	{
		this.keys.remove(msisdn);
	}

	public void unregisterKey(String msisdn)
	{
		unregisterKey(new MsIsdn(msisdn));
	}

	public void unregisterAllKeys()
	{
		this.keys.clear();
	}

	public AbstractKey getKey(MsIsdn msisdn)
	{
		return this.keys.get(msisdn);
	}

	public AbstractKey getKey(String msisdn)
	{
		return getKey(new MsIsdn(msisdn));
	}

	public byte[] encrypt(MsIsdn msisdn, byte[] message) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException
	{
		AbstractKey k = getKey(msisdn);
		if (k == null) throw new RuntimeException("Internal Error during Encryption - key not found #1!");
		else if (k instanceof AbstractSymmetricKey) return ((AbstractSymmetricKey) k).encrypt(message);
		else throw new RuntimeException("Internal Error during Encryption - key not found #2!");
	}

	public byte[] decrypt(MsIsdn msisdn, byte[] message) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException
	{
		AbstractKey k = getKey(msisdn);
		if (k == null) throw new RuntimeException("Internal Error during Decryption - key not found #1!");
		else if (k instanceof AbstractSymmetricKey) return ((AbstractSymmetricKey) k).decrypt(message);
		else throw new RuntimeException("Internal Error during Decryption - key not found #2!");
	}
}
