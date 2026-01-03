package repairExamples;

import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

public class Pattern6_SecureRandomConstantSeed {

   public static byte[] aes1(int mode, String password, byte[] textBytes) throws Exception{
      
      KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
      SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
      secureRandom.setSeed("some string".getBytes("utf-8"));
      keyGenerator.init(128, secureRandom);
      
      byte[] encoded = MessageDigest.getInstance("SHA-3").digest();
      SecretKeySpec secretKeySpec = new SecretKeySpec(encoded, "AES");
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(mode, secretKeySpec);
      
      return cipher.doFinal(textBytes);
  }
   
}