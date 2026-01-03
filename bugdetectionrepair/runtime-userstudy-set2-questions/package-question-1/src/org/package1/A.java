package org.package1;

import java.util.Base64; 

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class A {
   public void ma1(final String algorithm) {
      String strToEncrypt = "plaintext";
      String secret = "ssshhhh!!";
      ma2(algorithm, strToEncrypt, secret);
   }

   public static String ma2(String algorithm, final String strToEncrypt, final String secret) {
      try {
         byte[] key = secret.getBytes("UTF-8");
         SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
         Cipher cipher = Cipher.getInstance(algorithm);
         cipher.init(Cipher.ENCRYPT_MODE, secretKey);
         return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
      } catch (Exception e) {
         System.out.println("Error while encrypting: " + e.toString());
      }
      return null;
   }
}
