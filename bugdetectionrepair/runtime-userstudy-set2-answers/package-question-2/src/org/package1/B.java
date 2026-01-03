package org.package1;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class B {
    static byte[] hashTemplate(byte[] data, String algorithm) {
        if (data == null || data.length <= 0) 
		    return null;
		return hashTemplate(data, algorithm, false);
    }

    static byte[] hashTemplate(byte[] data, String algorithm, boolean logging) {
        try {
        	MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(data);
            if (logging == true) 
                System.out.println(md.digest());
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
} 