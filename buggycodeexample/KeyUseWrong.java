import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class KeyUseWrong {
	
	private static final String algorithm = "AES/CBC/PKCS5PADDING";
	private static final String plaintext = "FIREBUGS!FIREBUGS!FIREBUGS!";

	public static void main(String[] args) {
		try {
			// Generate a key from a hard coded string in 16 bytes
			String secret = "THISISASECRET!!!";
			SecretKeySpec keyspec = new SecretKeySpec(secret.getBytes(), "AES");
			// Sometimes a SHA function can be used to generate these 16 bytes from any string in arbitrary length
			
			// Generate a initialization vector
			byte[] iv = new byte[128 / 8];
			SecureRandom prng = new SecureRandom();
			prng.nextBytes(iv);

			// Select a cipher algorithm
			Cipher cipher = Cipher.getInstance(algorithm);

			// Encrypt
			cipher.init(Cipher.ENCRYPT_MODE, keyspec, new IvParameterSpec(iv));
			byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
			System.out.println(new String(ciphertext));
			
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
