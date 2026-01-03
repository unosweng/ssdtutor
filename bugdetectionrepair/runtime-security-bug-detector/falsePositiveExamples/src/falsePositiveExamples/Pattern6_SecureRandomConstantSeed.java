package falsePositiveExamples;

import java.security.SecureRandom;

public class Pattern6_SecureRandomConstantSeed {

   public void performAes() throws Exception {
      byte[] seed = SecureRandom.getSeed(55);
      aes1(1, "password", "textBytes".getBytes(), seed);
   }
   
   public void aes1(int mode, String password, byte[] textBytes, byte[] seed) throws Exception{
      
      SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
      secureRandom.setSeed(seed);
  }
   
}