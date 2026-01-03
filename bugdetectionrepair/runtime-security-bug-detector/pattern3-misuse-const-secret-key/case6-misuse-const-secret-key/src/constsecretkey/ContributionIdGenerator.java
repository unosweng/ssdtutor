/*******************************************************************************
 * Class extracted from MUBench Project 017_rcsjta
 ******************************************************************************/

package constsecretkey;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Contribution ID generator based on RFC draft-kaplan-dispatch-session-id-03
 * 
 * @author jexa7410
 */
public class ContributionIdGenerator {

    private static final String ALOGIRITHM_HMACSHA1 = "HmacSHA1";
    private static final String UTF8 = "UTF8";

    /**
     * Secret Key generator.
     */
    private static byte[] generateSecretKey() {
        final byte[] rawKey = generateRawKey();
        /**
         * Keep only 128 bits
         */
        byte[] secretKey = new byte[16];
        for (int i = 0; i < 16; i++) {
            if (rawKey != null && rawKey.length >= 16) {
                secretKey[i] = rawKey[i];
            } else {
                secretKey[i] = '0';
            }
        }
        return secretKey;
    }

    /**
     * Raw Key generator.
     */
    private static byte[] generateRawKey() {
        return UUID.randomUUID().toString().getBytes();
    }

    /**
     * Returns the Contribution ID
     * 
     * @param callId Call-ID header value
     * @return the Contribution ID
     * @throws UnsupportedEncodingException 
     * @throws IllegalStateException 
     */
    public synchronized static String getContributionId(String callId) throws IllegalStateException, UnsupportedEncodingException {
        try {
            // HMAC-SHA1 operation
            SecretKeySpec sks = new SecretKeySpec(generateSecretKey(), ALOGIRITHM_HMACSHA1);
            Mac mac = Mac.getInstance(ALOGIRITHM_HMACSHA1);
            mac.init(sks);
            byte[] contributionId = mac.doFinal(callId.getBytes(UTF8));

            // Convert to Hexa and keep only 128 bits
            StringBuilder hexString = new StringBuilder(32);
            for (int i = 0; i < 16 && i < contributionId.length; i++) {
                String hex = Integer.toHexString(0xFF & contributionId[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            String id = hexString.toString();
            return id;

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Not able to generate Contribution Id", e);

        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Not able to generate Contribution Id", e);
        }
    }
}
