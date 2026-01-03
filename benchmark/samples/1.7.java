public class A{
	
	public static String algorithm = "AES/CBC/PKCS5PADDING";

}

public class B{

	public void func(){
		Cipher cipher = Cipher.getInstance(A.algorithm);
	}

}
