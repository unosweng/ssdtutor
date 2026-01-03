public String ret(){
	String algorithm = "AES/CBC/PKCS5PADDING";
	return algorithm;
}

public void func(){
	String algorithm = ret();
	Cipher cipher = Cipher.getInstance(algorithm);
}
