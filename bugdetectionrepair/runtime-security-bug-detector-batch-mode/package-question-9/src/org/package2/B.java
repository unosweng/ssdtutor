package org.package2;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.StringTokenizer;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.package1.A;

public class B {
   private static final String CHARSET = "UTF8";
   private static final String PLAIN_TEXT = "plain text";

   static byte[] SALT = { //
         (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12, //
         (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12, //
   };

   public static void mb1(int cipherMode) throws Exception {
      StringTokenizer stok = new StringTokenizer(Arrays.toString(SALT));
      String b64Auth = stok.nextToken();
      String auth = new String(Base64.getDecoder().decode(b64Auth));

      if (auth.indexOf(':') != -1) {
         throw new IllegalArgumentException();
      }

      String algorithm = "AES/CBC/PKCS5PADDING";
      mb2(algorithm);
   }

   public static void mb2(final String algorithm) throws Exception {
      byte[] value = PLAIN_TEXT.getBytes((Charset.forName(CHARSET)));

      if (value == null || value.length < 2) {
         throw new IllegalArgumentException();
      } else {
         System.out.println(Arrays.toString(value) + ": " + //
               Base64.getEncoder().encodeToString(value) + ": " + //
               ByteBuffer.wrap(value).getLong());
      }
      A a = new A();
      a.ma1(algorithm);
   }

   public static byte[] mb3(byte[] data, String algorithm) throws Exception {
      if (data == null || data.length <= 0) {
         throw new IllegalArgumentException();
      }

      return mb4(data, algorithm, false);
   }

   static byte[] mb4(byte[] data, String algorithm, boolean logging) throws Exception {
      MessageDigest md = MessageDigest.getInstance("SHA3-512");
      md.update(data);

      if (logging == true) {
         System.out.println(md.digest());
      }
      return md.digest();
   }

   public String mb5(SecretKeySpec keySpec, String str) throws Exception {
      IvParameterSpec ivSpec = mb6();
      Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
      cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
      byte[] ciphertext = cipher.doFinal(str.getBytes());
      return Base64.getEncoder().encodeToString(ciphertext);
   }

   public static IvParameterSpec mb6() throws Exception {
      SecureRandom random = new SecureRandom();
      byte iv[] = new byte[16];
      random.nextBytes(iv);
      return new IvParameterSpec(iv);
   }

   public static void mb7() throws Exception {
      byte[] pbeKey = new String(SALT, CHARSET).getBytes();
      System.out.println(Arrays.toString(pbeKey) + ": " + //
            Base64.getEncoder().encodeToString(pbeKey) + ": " + //
            ByteBuffer.wrap(pbeKey).getLong());
      mb8(pbeKey, 1000);
   }

   public static void mb8(byte[] pbeKey, int count) throws Exception {
      PBEParameterSpec pbKey = new PBEParameterSpec(mb9(pbeKey), count);
      A a = new A();
      a.ma8("encrypt", pbKey);
   }

   public static byte[] mb9(byte[] varByte) {
      byte[] randomByte = varByte.clone();
      SecureRandom secureRandom = new SecureRandom();
      secureRandom.nextBytes(randomByte);
      return randomByte;
   }
}
