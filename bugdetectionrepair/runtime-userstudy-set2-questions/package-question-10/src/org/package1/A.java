package org.package1;

import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import org.package2.B;
import java.security.SecureRandom;

public class A {
   private static String algorithm = "AES/CBC/PKCS5PADDING";
   private static String plaintext = "this is the text";

   public String ma1(IvParameterSpec ivSpec) throws Exception {
      KeyGenerator keyGen = KeyGenerator.getInstance("AES");
      keyGen.init(256);
      SecretKey keyGenerator = keyGen.generateKey();

      Cipher cipher = Cipher.getInstance(algorithm);

      cipher.init(Cipher.ENCRYPT_MODE, keyGenerator, ivSpec);
      byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
      return Base64.getEncoder().encodeToString(ciphertext);
   }

   //

   public static void ma2() {
      String algo = "AES";
      ma3(algo);
   }

   public static void ma3(final String algorithm) {
      B b = new B();
      b.mb4(algorithm, "Encryptthis");
   }

   //

   private static String strToEncrypt = "encrypt";

   public String ma4(String algo) {
      String algorithm = "PBEWithMD5AndDES";

      try {
         SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algo);
         KeySpec spec = new DESKeySpec("12345678".getBytes());

         SecretKey key = keyFactory.generateSecret(spec);
         Cipher pbeCipher = Cipher.getInstance(algorithm);
         pbeCipher.init(Cipher.ENCRYPT_MODE, key);
         byte[] ciphertext = pbeCipher.doFinal(strToEncrypt.getBytes());
         return Base64.getEncoder().encodeToString(ciphertext);
      } catch (Exception e) {
         System.out.println("Error while encrypting: " + e.toString());
      }
      return null;
   }

   //

   public static void ma5() {
      byte[] initVec = "initializationvector".getBytes();
      ma6(initVec);
   }

   public static void ma6(byte[] iv) {
      IvParameterSpec ivSpec = new IvParameterSpec(iv);
      B b = new B();
      b.mb7("encrypt", ivSpec);
   }
}