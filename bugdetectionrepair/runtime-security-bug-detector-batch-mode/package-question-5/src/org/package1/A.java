package org.package1;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import java.security.SecureRandom;

public class A {
	private static String algorithm = "AES/CBC/PKCS5PADDING";
	private static String plaintext = "this is the text";
	
	public String m1(IvParameterSpec ivSpec) throws Exception {
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(256);
		SecretKey keyGenerator = keyGen.generateKey();

		Cipher cipher = Cipher.getInstance(algorithm);

		cipher.init(Cipher.ENCRYPT_MODE, keyGenerator, ivSpec);
		byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
		return Base64.getEncoder().encodeToString(ciphertext);
	}

}

