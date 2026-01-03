package constsecretkey;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class KeyUseCorrect2 {
	
	private static final String algorithm = "AES/CBC/PKCS5PADDING";
	private static final String plaintext = "FIREBUGS!FIREBUGS!FIREBUGS!";

	public static void main(String[] args) {
		try {
			// Assume this secret string is from the user
			String secret = "THISISASECRET";
			// Use PBKDF2 function
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			// Generate a salt
			byte[] salt = new byte[16];
			SecureRandom prng = new SecureRandom();
			prng.nextBytes(salt);
			// Derive the key from the secret string		
			KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt, 65536, 128);
			SecretKey derivedkey = factory.generateSecret(spec);
			// Transform the key to be usable by AES
			SecretKey key = new SecretKeySpec(derivedkey.getEncoded(), "AES");
			
			// Generate a initialization vector
			byte[] iv = new byte[128 / 8];
			prng = new SecureRandom();
			prng.nextBytes(iv);

			// Select a cipher algorithm
			Cipher cipher = Cipher.getInstance(algorithm);

			// Encrypt
			cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
			byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
			System.out.println(new String(ciphertext));
			
			// OK we have the question on where to save the salt and iv
			
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
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}

	}

}
