import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class A {
	public static void main(String[] args) throws Exception {
		String plaintext = "this is plaintext";
		final String secretKey = "ssshhhhhhhhh!!!!";
		
		String encryptedString = m1(plaintext, secretKey) ;
		System.out.println(encryptedString);
	}

	public static String m1(final String strToEncrypt, final String secret) {
		try {
			String algorithm = "AES/ECB/PKCS5PADDING";
			byte[] key = secret.getBytes("UTF-8");
			SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return Base64.getEncoder()
					.encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
		} catch (Exception e) {
			System.out.println("Error while encrypting: " + e.toString());
		}
		return null;
	}
}
