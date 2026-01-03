public class A{

	public void caller(){
		B inst = new B()
		String algorithm = "AES/CBC/PKCS5PADDING";
		inst.func(algorithm);
	}

}

public class B{

	public void func(String algorithm){
		Cipher cipher = Cipher.getInstance(algorithm);
	}

}
