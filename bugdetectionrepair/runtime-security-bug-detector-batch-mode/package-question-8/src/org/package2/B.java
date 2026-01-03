package org.package2;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class B {
	private static String algorithm = "AES/CBC/PKCS5Padding";
	
	public String m1(final String strToEncrypt, IvParameterSpec ivSpec) {
		try {
			byte[] key = new byte[8];
			SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
			
			Cipher cipher = Cipher.getInstance(algorithm);

			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
			byte[] ciphertext = cipher.doFinal(strToEncrypt.getBytes());
			return Base64.getEncoder().encodeToString(ciphertext);
		} catch (Exception e) {
			System.out.println("Error while encrypting: " + e.toString());
		}
		return null;
	}
}
