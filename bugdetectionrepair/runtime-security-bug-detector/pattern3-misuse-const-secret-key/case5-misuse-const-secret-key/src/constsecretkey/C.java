package constsecretkey;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class C {

    public static byte[] cm1(String parm) throws NoSuchAlgorithmException {

        return cm2(parm);

    }

    public static byte[] cm2(String parm) throws NoSuchAlgorithmException {

        MessageDigest hash = MessageDigest.getInstance(parm);

        return hash.digest();

    }

}