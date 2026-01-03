package constsecretkey;
import java.security.MessageDigest;

public class PwDatabaseV3 {
	public byte[] finalKey;

	public void makeFinalKey(byte[] masterSeed, byte[] masterSeed2, int numRounds) {
		MessageDigest md = null;
		// ...
		finalKey = md.digest();
	}
}
