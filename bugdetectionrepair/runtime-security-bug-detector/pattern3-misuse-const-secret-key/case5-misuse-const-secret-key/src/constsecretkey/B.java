package constsecretkey;

import java.security.NoSuchAlgorithmException;

class B {

    public static byte[] bm1(String parm) throws NoSuchAlgorithmException {

        byte[] key = C.cm1(parm);

        bm2();

        return key;

    }

    public static void bm2() {

        System.out.println("D.m4");

    }

}