package org.package2;

import javax.crypto.spec.PBEParameterSpec;

import org.package1.A;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;

public class B {
	private final static byte[] SALT = { //
			(byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12, //
			(byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12, //
	};
	
	public static void mb1(byte[] pbeKey, int count) throws UnsupportedEncodingException {
		mb2(SALT, 20);
	}

	public static void mb2(byte[] pbeKey, int count) throws UnsupportedEncodingException {
		PBEParameterSpec pbKey = new PBEParameterSpec(SALT, count);
		A a = new A();
		a.ma1("encrypt", pbKey);
	}

}
