package repairExamples;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Pattern1_ElectronicCodeBook {

    public byte[] decryptKey(final byte[] input) throws Exception {
        final Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(MessageDigest.getInstance("SHA-3").digest(), "AES"));
        return cipher.doFinal(input);
    }

}
