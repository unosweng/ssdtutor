package org.package2;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.SecureRandom;

public class B {
	private static String algorithm = "AES/CBC/PKCS5Padding";
	
	public static IvParameterSpec mb1() throws Exception {
		SecureRandom random = new SecureRandom();
		byte iv[] = new byte[16];
		random.nextBytes(iv);
		return new IvParameterSpec(iv);
	}
	
	public String mb2(SecretKeySpec secretKey, String strTom5) {
		try {
			IvParameterSpec ivSpec = mb1();
			Cipher cipher = Cipher.getInstance(algorithm);

			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
			byte[] ciphertext = cipher.doFinal(strTom5.getBytes());
			return Base64.getEncoder().encodeToString(ciphertext);
		} catch (Exception e) {
			System.out.println("Error while m5ing: " + e.toString());
		}
		return null;
	}
}
