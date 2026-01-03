import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.SecureRandom;

public class A {
	private static String algorithm = "AES/CBC/PKCS5Padding";

	public static void main(String[] args) throws Exception {
		String plaintext = "this is plaintext";
		String encrptString = m2(plaintext) ;
		System.out.println(encrptString);
		System.out.println(m3(plaintext));
		
	}
	
	public static IvParameterSpec m1() throws Exception {
		SecureRandom random = new SecureRandom();
		byte iv[] = new byte[16];
		random.nextBytes(iv);
		return new IvParameterSpec(iv);
	}
	
	public static String m2(final String strTom5) {
		try {
			String secret = "ssshhhhhhhhh!!!!";
			SecretKeySpec secretKey = new SecretKeySpec(getRandomSecretKey(secret.getBytes()), "AES");
			IvParameterSpec ivSpec = m1();
			Cipher cipher = Cipher.getInstance(algorithm);

			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
			byte[] ciphertext = cipher.doFinal(strTom5.getBytes());
			return Base64.getEncoder().encodeToString(ciphertext);
		} catch (Exception e) {
			System.out.println("Error while m5ing: " + e.toString());
		}
		return null;
	}

	public static String m3(final String strTom5) {
		try {
			byte[] key = new byte[8];
			SecretKeySpec secretKey = new SecretKeySpec(getRandomSecretKey(key), "AES");
			IvParameterSpec ivSpec = m1();
			Cipher cipher = Cipher.getInstance(algorithm);

			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
			byte[] ciphertext = cipher.doFinal(strTom5.getBytes());
			return Base64.getEncoder().encodeToString(ciphertext);
		} catch (Exception e) {
			System.out.println("Error while m5ing: " + e.toString());
		}
		return null;
	}

	public static byte[] getRandomSecretKey(byte[] VAR1) {
		byte[] VAR2 = VAR1.clone();
		SecureRandom secureRandom = new SecureRandom();
		secureRandom.nextBytes(VAR2);
		return VAR2;
	}
}
