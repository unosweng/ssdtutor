package electronicCodeBook;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException; 
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class NewEncryptWeak {

	private static final String algorithm = "AES/ECB/PKCS5PADDING";
	private static final String plaintext = "FIREBUGS!FIREBUGS!FIREBUGS!";

	public static void main(String[] args) {
		try {
			// Generate a key from a static string
			KeyGenerator keyGen = KeyGenerator.getInstance("DES");
			keyGen.init(128);
			SecretKey key = keyGen.generateKey();
			
			// Select a cipher algorithm
			Cipher cipher = Cipher.getInstance(algorithm);

			// Encrypt
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
			System.out.println(new String(ciphertext));

			// Decrypt
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] plainvalue = cipher.doFinal(ciphertext);
			String plaintext = new String(plainvalue);
			System.out.println(new String(plaintext));
			
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
		}
	}

	void foo() {
		final String algorithmInVar = "AES/ECB/PKCS5PADDING";
		try {
			Cipher cipher = Cipher.getInstance(algorithmInVar);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
	}

    void bar() {
       try {
           Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
       } catch (NoSuchAlgorithmException e) {
           e.printStackTrace();
       } catch (NoSuchPaddingException e) {
           e.printStackTrace();
       }
   }
    
   void baz(String arg1) {
      try {
         Cipher cipher = Cipher.getInstance(arg1);
     } catch (NoSuchAlgorithmException e) {
         e.printStackTrace();
     } catch (NoSuchPaddingException e) {
         e.printStackTrace();
     }
   }
}
