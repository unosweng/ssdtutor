/*
 * Copyright 2017 Brian Pellin, Jeremy Jamet / Kunzisoft.
 *     
 * This file is part of KeePass DX.
 *
 *  KeePass DX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  KeePass DX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with KeePass DX.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package constsecretkey;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class PwDatabase {

    public static final UUID UUID_ZERO = new UUID(0,0);

    protected byte masterKey[] = new byte[32];
    protected byte[] finalKey;

    public byte[] getMasterKey() {
        return masterKey;
    }

    public void setMasterKey(byte[] masterKey) {
        this.masterKey = masterKey;
    }

    public byte[] getFinalKey() {
        return finalKey;
    }

    protected abstract byte[] loadXmlKeyFile(InputStream keyInputStream);

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public boolean validatePasswordEncoding(String key) {
        if (key == null)
            return false;

        String encoding = getPasswordEncoding();

        byte[] bKey;
        try {
            bKey = key.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            return false;
        }

        String reencoded;

        try {
            reencoded = new String(bKey, encoding);
        } catch (UnsupportedEncodingException e) {
            return false;
        }

        return key.equals(reencoded);
    }

    protected abstract String getPasswordEncoding();

    public byte[] getPasswordKey(String key) throws IOException {
        if ( key == null)
            throw new IllegalArgumentException( "Key cannot be empty." ); // TODO

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 not supported");
        }

        byte[] bKey;
        try {
            bKey = key.getBytes(getPasswordEncoding());
        } catch (UnsupportedEncodingException e) {
            assert false;
            bKey = key.getBytes();
        }
        md.update(bKey, 0, bKey.length );

        return md.digest();
    }

    public abstract long getNumberKeyEncryptionRounds();

    public abstract void setNumberKeyEncryptionRounds(long rounds) throws NumberFormatException;


    /**
     * Determine if RecycleBin is available or not for this version of database
     * @return true if RecycleBin enable
     */
    protected boolean isRecycleBinAvailable() {
        return false;
    }

    /**
     * Determine if RecycleBin is enable or not
     * @return true if RecycleBin enable, false if is not available or not enable
     */
    protected boolean isRecycleBinEnabled() {
        return false;
    }

    /**
     * Initialize a newly created database
     */
    public abstract void initNew(String dbPath);

    public abstract void clearCache();

}
