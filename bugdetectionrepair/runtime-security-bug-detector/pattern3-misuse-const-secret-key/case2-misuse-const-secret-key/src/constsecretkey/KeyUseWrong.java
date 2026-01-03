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

   void baz(byte[] key, String algorithm) {
      try {
         SecretKeySpec keyspec = new SecretKeySpec(key, algorithm);
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

   public static byte[] m6(String data, String key) {
      byte[] tmp = key.getBytes();
      String tmp002;
      byte[] tmp1;
      tmp002 = "not related";
      tmp1 = tmp;
      return m1(data.getBytes(), tmp, "AES");
   }

   public static String m5(final String data, final String key) {
      if (data == null || data.length() == 0 || key == null || key.length() == 0)
         return "";
      return m4(data.getBytes(), key.getBytes());
   }

   private static String m3(byte[] encryptHmacSHA512) {
      return null;
   }

   public static String m4(final byte[] data, final byte[] key) {
      return m3(m2(data, key));
   }

   public static byte[] m2(final byte[] data, final byte[] key) {
      dummy1(key);
      dummy2(data);
      return m1(data, key, "HmacSHA512");
   }

   private static byte[] m1(final byte[] data, final byte[] key, final String algorithm) {
      if (data == null || data.length == 0 || key == null || key.length == 0)
         return null;
      try {
         SecretKeySpec secretKey = new SecretKeySpec(key, algorithm);
         Mac mac = Mac.getInstance(algorithm);
         mac.init(secretKey);
         return mac.doFinal(data);
      } catch (InvalidKeyException | NoSuchAlgorithmException e) {
         e.printStackTrace();
         return null;
      }
   }

   static void dummy1(final byte[] key) {
   }

   static void dummy2(final byte[] key) {
   }
}
