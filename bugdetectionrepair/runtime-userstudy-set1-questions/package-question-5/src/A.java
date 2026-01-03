import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import java.security.SecureRandom;

public class A {
	private static String algorithm = "AES/CBC/PKCS5PADDING"; 

	public static void main(String[] args) throws Exception {
		String plaintext = "this is plaintext";
		
		String encryptedString = m2(plaintext);
		System.out.println(encryptedString);
	}

	public static IvParameterSpec m1() throws Exception {
		SecureRandom random = new SecureRandom();
		byte iv[] = new byte[16];
		random.setSeed("12345".getBytes());
		random.nextBytes(iv);
		return new IvParameterSpec(iv);
	}
	
	public static String m2(String plaintext) throws Exception {
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(256);
		SecretKey keyGenerator = keyGen.generateKey();
		IvParameterSpec ivSpec = m1();

		Cipher cipher = Cipher.getInstance(algorithm);

		cipher.init(Cipher.ENCRYPT_MODE, keyGenerator, ivSpec);
		byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
		return Base64.getEncoder().encodeToString(ciphertext);
	}

}

