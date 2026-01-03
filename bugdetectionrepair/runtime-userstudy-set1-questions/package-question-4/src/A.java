import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

public class A {
	private static String algorithm = "PBEWithMD5AndDES";

	public static void main(String[] args) throws Exception {
		String plaintext = "this is plaintext";
		String encryptedString = m2(plaintext) ;
		System.out.println(encryptedString);
		System.out.println(m3(plaintext));
	}
	
	public static Key m1() {
		try {
			PBEKeySpec pbe = new PBEKeySpec("password".toCharArray());
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
			Key skey = keyFactory.generateSecret(pbe);
			return skey;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
		
	
	public static String m2(final String strToEncrypt) {
		try {
			Key key = m1();
			PBEParameterSpec pbeKey = new PBEParameterSpec("12345678".getBytes("UTF8"), 1000);
			Cipher pbeCipher = Cipher.getInstance(algorithm);
			pbeCipher.init(Cipher.ENCRYPT_MODE, key, pbeKey);
			byte[] ciphertext = pbeCipher.doFinal(strToEncrypt.getBytes());
			return Base64.getEncoder().encodeToString(ciphertext);
		} catch (Exception e) {
			System.out.println("Error while encrypting: " + e.toString());
		}
		return null;
	}
	
	public static String m3(final String strToEncrypt) {
		try {
			Key key = m1();
			SecureRandom random = new SecureRandom();
			byte[] bytes = new byte[8];
			random.nextBytes(bytes);
			PBEParameterSpec pbeKey = new PBEParameterSpec(bytes, 20);
			Cipher pbeCipher = Cipher.getInstance(algorithm);
			pbeCipher.init(Cipher.ENCRYPT_MODE, key, pbeKey);
			byte[] ciphertext = pbeCipher.doFinal(strToEncrypt.getBytes());
			return Base64.getEncoder().encodeToString(ciphertext);
		} catch (Exception e) {
			System.out.println("Error while encrypting: " + e.toString());
		}
		return null;
	}

}
