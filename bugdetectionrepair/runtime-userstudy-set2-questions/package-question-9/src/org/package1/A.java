package org.package1;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.package2.B;

public class A {
   private static final String CHARSET = "UTF8";

   static byte[] SALT = { //
         (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12, //
         (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12, //
   };

   public void ma1(final String algorithm) throws Exception {
      String strToEncrypt = "plaintext";
      String secret = "ssshhhh!!";
      ma2(algorithm, strToEncrypt, secret);
   }

   public static String ma2(String algorithm, final String strToEncrypt, final String secret) throws Exception {
      byte[] key = secret.getBytes("UTF-8");
      SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
      Cipher cipher = Cipher.getInstance(algorithm);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey);
      return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
   }

   void ma3(int cipherMode) throws Exception {
      int inversedMode = Cipher.ENCRYPT_MODE;

      if (cipherMode == Cipher.DECRYPT_MODE)
         inversedMode = Cipher.ENCRYPT_MODE;
      else if (cipherMode == Cipher.ENCRYPT_MODE)
         inversedMode = Cipher.DECRYPT_MODE;
      else
         throw new UnsupportedOperationException("[WRN] " + inversedMode);

      byte[] data = new String(SALT, CHARSET).getBytes();
      String algorithm = "SHA3-512";
      ma4(data, algorithm);
   }

   byte[] ma4(byte[] data, String algorithm) throws Exception {
      if (data == null || data.length <= 0)
         return null;
      if (algorithm == null || algorithm.isEmpty())
         return null;
      return B.mb3(data, algorithm);
   }

   public static void ma5() throws Exception {
      byte[] secret = new String(SALT, CHARSET).getBytes();
      String algorithm = "AES";
      ma6(secret, algorithm);
   }

   public static void ma6(byte[] secret, String algorithm) throws Exception {
      SecretKeySpec keySpec = new SecretKeySpec(ma7(secret), algorithm);
      B b = new B();
      b.mb5(keySpec, "encryptthis");
   }

   public static byte[] ma7(byte[] VAR1) {
      byte[] VAR2 = VAR1.clone();
      SecureRandom secureRandom = new SecureRandom();
      secureRandom.nextBytes(VAR2);
      return VAR2;
   }

   public String ma7(final String strToEncrypt, PBEParameterSpec pbeKey) throws Exception {
      Key key = ma9();
      Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
      pbeCipher.init(Cipher.ENCRYPT_MODE, key, pbeKey);
      byte[] ciphertext = pbeCipher.doFinal(strToEncrypt.getBytes());
      return Base64.getEncoder().encodeToString(ciphertext);
   }

   public String ma8(final String strToEncrypt, PBEParameterSpec pbeKey) throws Exception {
      Key key = ma9();
      SecureRandom random = new SecureRandom();
      byte[] bytes = new byte[8];
      random.nextBytes(bytes);
      Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
      pbeCipher.init(Cipher.ENCRYPT_MODE, key, pbeKey);
      byte[] ciphertext = pbeCipher.doFinal(strToEncrypt.getBytes());
      return Base64.getEncoder().encodeToString(ciphertext);
   }

   public Key ma9() throws Exception {
      PBEKeySpec pbe = new PBEKeySpec("password".toCharArray());
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("RSA/ECB/PKCS1Padding");
      Key skey = keyFactory.generateSecret(pbe);
      return skey;
   }
}
