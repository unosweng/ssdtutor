package org.package1;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.package2.B;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class A {
	public static void ma1() throws Exception {
		byte[] secret = "12345".getBytes();
		String algorithm = "AES";
		ma2(secret, algorithm);
	}
	
	public static void ma2(byte[] profileKey, String algorithm) {
	    try {
	      byte[]         nonce  = new byte[12];
	      byte[]         input  = new byte[16];

	      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
	      cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(profileKey, "AES"), new GCMParameterSpec(128, nonce));

	      byte[] ciphertext = cipher.doFinal(input);
	    } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException e) {
	      throw new AssertionError(e);
	    }
	  }
}
