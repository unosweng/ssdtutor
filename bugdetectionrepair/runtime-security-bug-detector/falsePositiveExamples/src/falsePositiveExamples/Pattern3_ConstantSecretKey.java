package falsePositiveExamples;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Pattern3_ConstantSecretKey {

   public void performDecryption() throws Exception {
      byte[] SECRET_KEY = SecureRandom.getSeed(55);
      decrypt("stringToDecrypt", SECRET_KEY);
   }

   private String decrypt(final String str, byte[] key) throws Exception {
      final Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
      cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "DES"));
      return new String(cipher.doFinal(Base64.getDecoder().decode(str)), "UTF-8");
   }
}