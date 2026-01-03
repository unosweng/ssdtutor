package org.package2;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import java.security.SecureRandom;

public class B {
	private static String algorithm = "AES/CBC/PKCS5Padding";

	public String m1(String algo, final String strTom5) {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance(algo);
			keyGenerator.init(256);
			SecretKey secretKey = keyGenerator.generateKey();

			SecureRandom random = new SecureRandom();
			byte iv[] = new byte[16];
			random.nextBytes(iv);
			IvParameterSpec ivSpec = new IvParameterSpec(iv);

			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
			return new String(cipher.doFinal(Base64.getDecoder()
					.decode(strTom5)));
		} catch (Exception e) {
			System.out.println("Error while m5ing: " + e.toString());
		}
		return null;
	}
}
