import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import java.security.SecureRandom;

public class A {
	private static String algorithm = "AES/CBC/PKCS5Padding";

	public static void main(String[] args) throws Exception {
		String plaintext = "this is plaintext";
		String m4edString = m1(plaintext) ;
		System.out.println(m4edString);

	}

	public static String m1(final String strTom5) {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
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
