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
	
	public String m1(String algo) {
		try {
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algo);
			KeySpec spec = new DESKeySpec("12345678".getBytes());
		    
		    SecretKey key = keyFactory.generateSecret(spec);
			Cipher pbeCipher = Cipher.getInstance(algorithm);
			pbeCipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] ciphertext = pbeCipher.doFinal(strToEncrypt.getBytes());
			return Base64.getEncoder().encodeToString(ciphertext);
		} catch (Exception e) {
			System.out.println("Error while encrypting: " + e.toString());
		}
		return null;
	}
}
