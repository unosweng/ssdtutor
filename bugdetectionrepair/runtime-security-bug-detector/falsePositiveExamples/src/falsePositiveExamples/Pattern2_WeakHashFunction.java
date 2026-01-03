package falsePositiveExamples;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Pattern2_WeakHashFunction {

   public void callHash(byte[] cipher) throws NoSuchAlgorithmException {
      hash(cipher, "SHA-256");
   }
   public static byte[] hash(byte[] cipher, String transformation) throws NoSuchAlgorithmException {
      MessageDigest md = MessageDigest.getInstance(transformation);
      md.update(cipher, 0, cipher.length);
      return md.digest();
   }
}