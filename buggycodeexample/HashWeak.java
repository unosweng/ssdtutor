import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashWeak {

	private static final String plaintext = "FIREBUGS!FIREBUGS!FIREBUGS!";
	
	public static void main(String[] args) {

		try {
			// Hash
			MessageDigest digest = MessageDigest.getInstance("SHA-1"); // it can also be "MD5"
			digest.update(plaintext.getBytes());
			byte[] hashValue = digest.digest();
			System.out.println(new String(hashValue));

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
}