package org.package1;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.spec.KeySpec;

public class A {
	private static String algorithm = "PBEWithMD5AndDES";
	private static String strToEncrypt = "encrypt";
	
	public Cipher m1(String algo) {
		try {
			KeySpec spec = new DESKeySpec("12345678".getBytes());
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algo);
		    SecretKey key = keyFactory.generateSecret(spec);
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return cipher;
		} catch (Exception e) {
			System.out.println("Error while encrypting: " + e.toString());
		}
		return null;
	}
}
