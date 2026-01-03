package org.package1;

import org.package2.B;

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
		B b = new B();
		return b.hashTemplate(data, algorithm);
    }
}