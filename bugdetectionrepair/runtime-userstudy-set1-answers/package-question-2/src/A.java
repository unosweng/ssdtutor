import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class A {
	public static void main(String[] args) throws Exception {
		m1("encrypt");
	}
	
	public static void m1(String strToEncrypt) throws  NoSuchAlgorithmException{
		String algo = "SHA3-512";
		MessageDigest m = MessageDigest.getInstance(algo);
		m.update(strToEncrypt.getBytes());
		byte[] md = m.digest();
		StringBuffer hexString = new StringBuffer();

		for (int i = 0;i<md.length;i++) {
			hexString.append(Integer.toHexString(0xFF & md[i]));
		}
		System.out.println("Output : " + hexString.toString());
	}
}
