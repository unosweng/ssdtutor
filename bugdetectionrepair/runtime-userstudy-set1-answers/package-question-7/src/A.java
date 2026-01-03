import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.spec.KeySpec;

public class A {
	private static String algorithm = "PBEWithMD5AndDES";

	public static void main(String[] args) throws Exception {
		String plaintext = "this is plaintext";
		String encryptedString = m1(plaintext) ;
		System.out.println(encryptedString);
	}
	
	public static String m1(final String strToEncrypt) {
		try {
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("AES");
			// InvalidKeySpecException
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
