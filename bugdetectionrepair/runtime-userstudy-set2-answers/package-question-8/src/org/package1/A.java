package org.package1;

import javax.crypto.spec.IvParameterSpec;

import org.package2.B;

public class A {
	public static void m1() {
		byte[] initVec = "initializationvector".getBytes();
		m2(initVec);
	}
	
	public static void m2(byte[] iv) {
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		B b = new B();
		b.m1("encrypt", ivSpec);
	}
	
}
