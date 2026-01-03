import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class KeyUseCorrect1 {
	
	private static final String algorithm = "AES/CBC/PKCS5PADDING";
	private static final String plaintext = "FIREBUGS!FIREBUGS!FIREBUGS!";

	public static void main(String[] args) {
		try {
			// Generate a key
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(128);
			SecretKey key = keyGen.generateKey();

			// Generate a initialization vector
			byte[] iv = new byte[128 / 8];
			SecureRandom prng = new SecureRandom();
			prng.nextBytes(iv);

			// Select a cipher algorithm
			Cipher cipher = Cipher.getInstance(algorithm);

			// Encrypt
			cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
			byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
			System.out.println(new String(ciphertext));
			
			// OK we have the question on where to save the iv
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
	}
}
