import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class HashWeak {

	private static final String plaintext = "FIREBUGS!FIREBUGS!FIREBUGS!";

	public static void main(String[] args) {
		try {
			// Hash
			MessageDigest digest = MessageDigest.getInstance("SHA3-512"); // it can also be "MD5"
			digest.update(plaintext.getBytes());
			byte[] hashValue = digest.digest();
			System.out.println(new String(hashValue));
			Cipher eax = Cipher.getInstance("AES/EAX/NoPadding", "SC");
	        final SecretKeySpec key = new SecretKeySpec(new byte[eax.getBlockSize()], eax.getAlgorithm());
	        Mac mac = Mac.getInstance("AES/EAX/NoPadding", "SC");

            mac.init(new SecretKeySpec(new byte[mac.getMacLength()], mac.getAlgorithm()), new IvParameterSpec(
                new byte[16]));

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void foo() {
		try {
			// Hash
			String algorithmInVar = "SHA3-512";
			MessageDigest digest = MessageDigest.getInstance(algorithmInVar); // it can also be "MD5"
			digest.update(plaintext.getBytes());
			byte[] hashValue = digest.digest();
			System.out.println(new String(hashValue));

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	String algorithmInField = "SHA-1";
	
	void bar() {
		try {
			// Hash
			MessageDigest digest = MessageDigest.getInstance(algorithmInField); // it can also be "MD5"
			digest.update(plaintext.getBytes());
			byte[] hashValue = digest.digest();
			System.out.println(new String(hashValue));

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	void baz(String algorithmInPara) {
		try {
			// Hash
			MessageDigest digest = MessageDigest.getInstance(algorithmInPara); // it can also be "MD5"
			digest.update(plaintext.getBytes());
			byte[] hashValue = digest.digest();
			System.out.println(new String(hashValue));

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public static byte[] getRandomSecretKey(byte[] VAR1) {
		byte[] VAR2 = VAR1.clone();
		SecureRandom secureRandom = new SecureRandom();
		secureRandom.nextBytes(VAR2);
		return VAR2;
	}
}