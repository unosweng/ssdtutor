package org.package2;

import org.package1.A;

public class B {
	public static void mb1() {
		String algorithm = "AES/CBC/PKCS5PADDING";
		mb2(algorithm);
	}
	
	public static void mb2(final String algorithm) {
		A a = new A();
		a.ma1(algorithm);
	}
}
