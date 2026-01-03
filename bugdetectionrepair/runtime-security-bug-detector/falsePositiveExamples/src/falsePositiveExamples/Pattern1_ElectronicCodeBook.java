package falsePositiveExamples;

import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Pattern1_ElectronicCodeBook {

   public String ENCODING = "AES/ECB/NoPadding";
   
   // Case 1: DONE - backtracking to a method invocation with a StringLiteral argument
   public void callDecryptKey1() throws Exception {
      decryptKey("message".getBytes(), "AES/ECB/NoPadding");
   }

   // Case 2: DONE - backtracking to a method invocation with a SimpleName argument
   public void callDecryptKey2() throws Exception {
      String enc = "AES/ECB/NoPadding";
      decryptKey("message".getBytes(), enc);
   }
   
   // Case 3: DONE - backtracking to a method invocation with another method invocation
   public void callDecryptKey3() throws Exception {
      callDecryptKey3a();
   }
   public void callDecryptKey3a() throws Exception {
      decryptKey("message".getBytes(), "AES/ECB/NoPadding");
   }

   // Case 4: member field used
   public void callDecryptKey4() throws Exception {
      decryptKey("message".getBytes(), ENCODING);
   }
   
   // Indicator method
   public byte[] decryptKey(final byte[] input, String transformation) throws Exception {
      final Cipher cipher = Cipher.getInstance(transformation);
      cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(MessageDigest.getInstance("SHA-3").digest(), "AES"));
      return cipher.doFinal(input);
   }
}