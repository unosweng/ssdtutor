package constsecretkey;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class KeyUseWrong {

   private static final String algorithm = "AES/CBC/PKCS5PADDING";
   private static final String plaintext = "FIREBUGS!FIREBUGS!FIREBUGS!";
   private static final String secretInField = "THISISASECRET!!!";

   protected PwDatabaseV3 createDB() {
      return new PwDatabaseV3();
   }

   void returnedVariable() {
      try {
         byte[] finalKey = getFinalKey();

         SecretKeySpec keyspec = new SecretKeySpec(finalKey, "AES");
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

   private PwDatabaseV3 mPM;

   private byte[] getFinalKey() {
      mPM.makeFinalKey(null, null, 0);
      return mPM.finalKey;
   }
}
