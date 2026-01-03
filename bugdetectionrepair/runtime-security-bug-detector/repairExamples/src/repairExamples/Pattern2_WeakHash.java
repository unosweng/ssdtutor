package repairExamples;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Pattern2_WeakHash {

   public static byte[] hash(byte[] cipher) throws NoSuchAlgorithmException {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      md.update(cipher, 0, cipher.length);
      return md.digest();
   }
}