package constsecretkey;

import java.io.IOException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PwDatabaseV3 {
    protected byte[] finalKey;
    protected byte masterKey[] = new byte[32];

    public byte[] getFinalKey() {
        return finalKey;
    }

    @SuppressWarnings("resource")
    public void makeFinalKey(byte[] masterSeed, byte[] masterSeed2, long numRounds) throws IOException {

        // Write checksum Checksum
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 not implemented here.");
        }
        NullOutputStream nos = new NullOutputStream();
        DigestOutputStream dos = new DigestOutputStream(nos, md);

        byte[] transformedMasterKey = transformMasterKey(masterSeed2, masterKey, numRounds);
        dos.write(masterSeed);
        dos.write(transformedMasterKey);

        finalKey = md.digest();
    }

    private static byte[] transformMasterKey(byte[] pKeySeed, byte[] pKey, long rounds) throws IOException {
        FinalKey key = FinalKeyFactory.createFinalKey();

        return key.transformMasterKey(pKeySeed, pKey, rounds);
    }

}
