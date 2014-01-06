
package org.smslib.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public abstract class AbstractSymmetricKey extends AbstractKey
{
	private SecretKeySpec key;

	public SecretKeySpec getKey()
	{
		return this.key;
	}

	public void setKey(SecretKeySpec key)
	{
		this.key = key;
	}

	public abstract SecretKeySpec generateKey() throws NoSuchAlgorithmException;

	public abstract byte[] encrypt(byte[] message) throws NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException;

	public abstract byte[] decrypt(byte[] message) throws NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException;
}
