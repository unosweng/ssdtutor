import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class A {
	private static String algorithm = "AES/CBC/PKCS5Padding";

	public static void main(String[] args) throws Exception {
		String plaintext = "this is plaintext";
		String encryptedString = m1(plaintext);
		System.out.println(encryptedString);
		
	}
	
	public static String m1(final String strToEncrypt) {
		try {
			byte[] key = new byte[8];
			SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
			
			String initVec = "initializationvector";
			byte iv[] = initVec.getBytes();
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
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
