package repairExamples;

import java.nio.charset.Charset;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Pattern3_ConstantSecretKey {

   private String decryptA(final String str) throws Exception {
      final Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
      cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec("38346591".getBytes(Charset.forName("UTF-8")), "DES"));
      return new String(cipher.doFinal(Base64.getDecoder().decode(str)), "UTF-8");
   }

   private String decryptB(final String str) throws Exception {
      final Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
      byte[] SECRET_KEY = "19564383".getBytes(Charset.forName("UTF-8"));
      cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(SECRET_KEY, "DES"));
      return new String(cipher.doFinal(Base64.getDecoder().decode(str)), "UTF-8");
   }
}