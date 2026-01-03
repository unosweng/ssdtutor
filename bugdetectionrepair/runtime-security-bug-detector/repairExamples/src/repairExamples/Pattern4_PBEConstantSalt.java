package repairExamples;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class Pattern4_PBEConstantSalt {
   
   private static final byte[] SALT = { (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12, (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12, };

   public static String decrypt(String cryptedString) {
      if (cryptedString == null || cryptedString.equals("")) {
         return "";
      }
      
      try {
         SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
         SecretKey key = keyFactory.generateSecret(new PBEKeySpec(null));
         Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
         pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 1000));
         return new String(pbeCipher.doFinal(base64Decode(cryptedString)), "UTF-8");
      } catch (Exception e) {
         throw new RuntimeException("failed to decrypt", e);
      }
   }

   private static byte[] base64Decode(String string) {
      return Base64.getDecoder().decode(string.getBytes());
   }

   public void test() {
      new PBEParameterSpec(new byte[20], 1000);
   }
}
