package org.package2;

import org.package1.A;

public class B {
	public void m1() {
		String algo = "DES";
		m2(algo);
	}
	
	public void m2(String algorithm) {
		A a = new A();
		a.m1(algorithm);
	}
}
