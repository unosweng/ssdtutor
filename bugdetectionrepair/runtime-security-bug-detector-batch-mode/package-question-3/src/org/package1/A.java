package org.package1;

import javax.crypto.spec.SecretKeySpec;

import org.package2.B;
import java.security.SecureRandom;

public class A {
	public static void ma1() throws Exception {
		byte[] secret = "12345".getBytes();
		String algorithm = "AES";
		ma2(secret, algorithm);
	}
	
	public static void ma2(byte[] secret, String algorithm) throws Exception {
		SecretKeySpec sSpec = new SecretKeySpec(getRandomSecretKey(secret), algorithm);
		B b = new B();
		b.mb2(sSpec, "encryptthis");
	}

	public static byte[] getRandomSecretKey(byte[] VAR1) {
		byte[] VAR2 = VAR1.clone();
		SecureRandom secureRandom = new SecureRandom();
		secureRandom.nextBytes(VAR2);
		return VAR2;
	}
}
