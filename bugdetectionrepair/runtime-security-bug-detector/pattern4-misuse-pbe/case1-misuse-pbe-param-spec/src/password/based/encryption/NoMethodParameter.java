package password.based.encryption;

import javax.crypto.spec.PBEParameterSpec;

public class NoMethodParameter {

    private static final byte[] SALT = { 0x0A, 0x02, 0x13, 0x3C, 0x3B, 0x0F, 0x1A };
    private static final int COUNT = 10;

    private static final String TEST = "hello123";

    void m1() {
        PBEParameterSpec pbe = new PBEParameterSpec(new byte[20], 10);
    }

    void m2() {
        String salt = "abc123";
        PBEParameterSpec pbe = new PBEParameterSpec(salt.getBytes(), 10);
    }

    void m3() {
        String salt = "abc123";
        PBEParameterSpec pbe = new PBEParameterSpec(salt.getBytes(), power(10, 7));
    }

    void m4() {
        PBEParameterSpec pbe = new PBEParameterSpec(SALT, COUNT);
    }

    int power(int x, int y) 
    { 
        if (y == 0) 
            return 1; 
        else if (y % 2 == 0) 
            return power(x, y / 2) * power(x, y / 2); 
        else
            return x * power(x, y / 2) * power(x, y / 2); 
    } 
    
}
