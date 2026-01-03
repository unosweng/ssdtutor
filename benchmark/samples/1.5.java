public class A{

	public String ret(){
		String algorithm = "AES/CBC/PKCS5PADDING";
		return algorithm;
	}

} 

public class B{

	A inst = new A();
	
	public void func(){
		String algorithm = A.ret();
		Cipher cipher = Cipher.getInstance(algorithm);
	}

}
