package org.package1;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class A {

   public String ma1(final String strToEncrypt, PBEParameterSpec pbeKey) {
      try {
         Key key = ma3();
         Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
         pbeCipher.init(Cipher.ENCRYPT_MODE, key, pbeKey);
         byte[] ciphertext = pbeCipher.doFinal(strToEncrypt.getBytes());
         return Base64.getEncoder().encodeToString(ciphertext);
      } catch (Exception e) {
         System.out.println("Error while encrypting: " + e.toString());
      }
      return null;
   }

   public static Key ma3() {
      try {
          PBEKeySpec pbe = new PBEKeySpec("password".toCharArray());
          SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
          Key skey = keyFactory.generateSecret(pbe);
          return skey;
      } catch (NoSuchAlgorithmException e) {
          e.printStackTrace();
      } catch (InvalidKeySpecException e) {
          e.printStackTrace();
      }
      return null;
  }
}
