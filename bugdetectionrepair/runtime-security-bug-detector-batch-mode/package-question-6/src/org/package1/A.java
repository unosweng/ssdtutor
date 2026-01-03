package org.package1;

import org.package2.B;

public class A {
	public static void m1() {
		String algo = "AES";
		m2(algo);
	}
	
	public static void m2(final String algorithm) {
		B b = new B();
		b.m1(algorithm, "Encryptthis");
	}
}
