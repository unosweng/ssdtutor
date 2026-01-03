package org.package2;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.package1.A;

public class B {
   public static void mb1() throws Exception {
      byte[] seed = mb2("!2345".getBytes());
      mb3(seed);
   }

   public static byte[] mb2(byte[] value) {
      byte[] valuecloned = value.clone();
      SecureRandom secureRandom = new SecureRandom();
      secureRandom.nextBytes(valuecloned);
      return valuecloned;
   }

   public static void mb3(byte[] seed) throws Exception {
      SecureRandom random = new SecureRandom();
      byte iv[] = new byte[16];
      random.setSeed(seed);
      random.nextBytes(iv);
      A a = new A();
      a.ma1(new IvParameterSpec(iv));
   }

   //

   public String mb4(String algo, final String strTom5) {
      String algorithm = "AES/CBC/PKCS5Padding";

      try {
         KeyGenerator keyGenerator = KeyGenerator.getInstance(algo);
         keyGenerator.init(256);
         SecretKey secretKey = keyGenerator.generateKey();

         SecureRandom random = new SecureRandom();
         byte iv[] = new byte[16];
         random.nextBytes(iv);
         IvParameterSpec ivSpec = new IvParameterSpec(iv);

         Cipher cipher = Cipher.getInstance(algorithm);
         cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
         return new String(cipher.doFinal(Base64.getDecoder().decode(strTom5)));
      } catch (Exception e) {
         System.out.println("Error while m5ing: " + e.toString());
      }
      return null;
   }

   //

   public void mb5() {
      String algo = "AES";
      mb6(algo);
   }

   public void mb6(String algorithm) {
      A a = new A();
      a.ma4(algorithm);
   }

   //

   private static String algorithm = "AES/CBC/PKCS5Padding";

   public String mb7(final String strToEncrypt, IvParameterSpec ivSpec) {
      try {
         byte[] key = new byte[8];
         SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

         Cipher cipher = Cipher.getInstance(algorithm);

         cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
         byte[] ciphertext = cipher.doFinal(strToEncrypt.getBytes());
         return Base64.getEncoder().encodeToString(ciphertext);
      } catch (Exception e) {
         System.out.println("Error while encrypting: " + e.toString());
      }
      return null;
   }
}
