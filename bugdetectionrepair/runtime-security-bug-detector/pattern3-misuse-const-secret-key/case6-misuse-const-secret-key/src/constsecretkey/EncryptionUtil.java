package constsecretkey;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtil {

   private static final String algorithm = "AES/CBC/PKCS5PADDING";
   private static final String plaintext = "FIREBUGS!FIREBUGS!FIREBUGS!";

   void encryptPlainText() {
      try {
         PwDatabaseV3 newManager;
         newManager = createDBv4();

         SecretKeySpec keyspec = new SecretKeySpec(newManager.getFinalKey(), "AES");
         byte[] iv = new byte[128 / 8];
         SecureRandom prng = new SecureRandom();
         prng.nextBytes(iv);
         Cipher cipher = Cipher.getInstance(algorithm); // Select a cipher algorithm
         cipher.init(Cipher.ENCRYPT_MODE, keyspec, new IvParameterSpec(iv)); // Encrypt
         byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
         System.out.println(new String(ciphertext));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   void openDatabase() throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException {
      PwDatabaseV5 databaseToOpen;
      databaseToOpen = createDBv5();
      Cipher cipher;
      cipher = CipherFactory.getInstance("AES/CBC/PKCS5Padding");
      cipher.init( Cipher.DECRYPT_MODE, new SecretKeySpec( databaseToOpen.getFinalKey(), "AES" ), new IvParameterSpec( new byte[16] ) );
   }

   
   protected PwDatabaseV3 createDBv4() {
      return new PwDatabaseV3();
   }

   protected PwDatabaseV5 createDBv5() {
      return new PwDatabaseV5();
   }

}
