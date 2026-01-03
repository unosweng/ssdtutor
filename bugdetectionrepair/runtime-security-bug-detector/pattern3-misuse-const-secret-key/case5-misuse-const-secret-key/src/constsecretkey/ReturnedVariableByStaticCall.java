package constsecretkey;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.spec.SecretKeySpec;

public class ReturnedVariableByStaticCall {

    public static class SecretKey {
        public static void main(String[] args) throws NoSuchAlgorithmException {
            
            byte[] key = "ABC123".getBytes();
            String algorithm = "HmacSHA256";

            byte[] blockKey = Message.m1(key);
            SecretKeySpec signingKey = new SecretKeySpec(blockKey, algorithm);
            System.out.println("signingKey: " + signingKey.hashCode());
        }
    }

    public static class Message {
        
        public static byte[] m1(byte[] key) throws NoSuchAlgorithmException {
            byte[] newKey = m2(key);
            m3();
            return newKey;
        }

        public static byte[] m2(byte[] key) throws NoSuchAlgorithmException {
            MessageDigest hash = MessageDigest.getInstance("SHA-512");
            return hash.digest();
        }

        public static byte[] m3() {
            System.out.println("m3");
            return "m3".getBytes();
        }
    }
}
