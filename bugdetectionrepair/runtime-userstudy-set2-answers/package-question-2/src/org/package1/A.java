package org.package1;

public class A {
    void encryptMD5() {
        byte[] data = "1234".getBytes();
        String algorithm = "SHA3-512";
        encryptHash(data, algorithm);
    }

    byte[] encryptHash(byte[] data, String algorithm) {
        if (data == null || data.length <= 0) 
		    return null;
		if (algorithm == null || algorithm.isEmpty()) 
		    return null; 
		return B.hashTemplate(data, algorithm);
    }
}