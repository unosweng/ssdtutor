public void caller(){
	String algorithm = "AES/CBC/PKCS5PADDING";
	func(algorithm);
}

public void func(String algorithm){
	Cipher cipher = Cipher.getInstance(algorithm);
}
