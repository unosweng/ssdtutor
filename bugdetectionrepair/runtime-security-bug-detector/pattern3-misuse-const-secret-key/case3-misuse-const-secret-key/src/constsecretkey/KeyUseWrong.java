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

public class KeyUseWrong {

   private static final String algorithm = "AES/CBC/PKCS5PADDING";
   private static final String plaintext = "FIREBUGS!FIREBUGS!FIREBUGS!";
   private static final String secretInField = "THISISASECRET!!!";

   void objMemberField() {
      try {
         PwDatabaseV3 newManager;
         newManager = createDB();

         SecretKeySpec keyspec = new SecretKeySpec(newManager.finalKey, "AES");
         byte[] iv = new byte[128 / 8];
         SecureRandom prng = new SecureRandom();
         prng.nextBytes(iv);
         Cipher cipher = Cipher.getInstance(algorithm); // Select a cipher
                                                        // algorithm
         cipher.init(Cipher.ENCRYPT_MODE, keyspec, new IvParameterSpec(iv)); // Encrypt
         byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
         System.out.println(new String(ciphertext));

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   protected PwDatabaseV3 createDB() {
      return new PwDatabaseV3();
   }
 }
