package stream;

import java.io.IOException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HmacBlockStream {
    public static byte[] GetHmacKey64(byte[] key, long blockIndex) {
        MessageDigest hash;
        try {
            hash = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        NullOutputStream nos = new NullOutputStream();
        DigestOutputStream dos = new DigestOutputStream(nos, hash);
        LEDataOutputStream leos = new LEDataOutputStream(dos);

        try {
            leos.writeLong(blockIndex);
            leos.write(key);
            leos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] hashKey = hash.digest();
        assert(hashKey.length == 64);

        return hashKey;
    }

}
