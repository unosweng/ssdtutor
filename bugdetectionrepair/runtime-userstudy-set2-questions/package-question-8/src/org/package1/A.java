package org.package1;

import javax.crypto.spec.IvParameterSpec;

import org.package2.B;
import java.security.SecureRandom;

public class A {
	public static void m1() {
		byte[] initVec = "initializationvector".getBytes();
		m2(initVec);
	}
	
	public static void m2(byte[] iv) {
		IvParameterSpec ivSpec = new IvParameterSpec(getRandomSecretKey(iv));
		B b = new B();
		b.m1("encrypt", ivSpec);
	}

	public static byte[] getRandomSecretKey(byte[] VAR1) {
		byte[] VAR2 = VAR1.clone();
		SecureRandom secureRandom = new SecureRandom();
		secureRandom.nextBytes(VAR2);
		return VAR2;
	}
	
}
