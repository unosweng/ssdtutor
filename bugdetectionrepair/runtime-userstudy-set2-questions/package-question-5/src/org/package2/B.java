package org.package2;
import javax.crypto.spec.IvParameterSpec;

import org.package1.A;

import java.security.SecureRandom;

public class B {
	public static void m1() throws Exception {
		byte[] seed = "!2345".getBytes();
		m2(seed);
	}

	public static void m2(byte[] seed) throws Exception {
		SecureRandom random = new SecureRandom();
		byte iv[] = new byte[16];
		random.setSeed(seed);
		random.nextBytes(iv);
		A a = new A();
		a.m1(new IvParameterSpec(iv));
	}
}

