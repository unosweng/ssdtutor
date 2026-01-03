package constsecretkey;

import java.security.NoSuchAlgorithmException;

import javax.crypto.spec.SecretKeySpec;

class A {

    static String SHA = "sha";

    void am1(byte[] parm) throws NoSuchAlgorithmException {

        byte[] key = B.bm1(SHA);

        SecretKeySpec signingKey = new SecretKeySpec(key, SHA);
        System.out.println(signingKey);

    }

}