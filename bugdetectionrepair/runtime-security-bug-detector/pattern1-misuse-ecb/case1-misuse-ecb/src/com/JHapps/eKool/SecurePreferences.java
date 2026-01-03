package com.JHapps.eKool;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

public class SecurePreferences {

    public static class SecurePreferencesException extends RuntimeException {

        /**
         * 
         */
        private static final long serialVersionUID = 3309458080755305896L;

        public SecurePreferencesException(Throwable e) {
            super(e);
        }

    }

    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String KEY_TRANSFORMATION = "AES/ECB/PKCS5Padding";

    private Cipher writer;
    private Cipher reader;
    private Cipher keyWriter;
    private String name;
    private boolean encrypt = false;

    public void setSecurePreferences(String preferenceName, boolean encryptKeys) throws SecurePreferencesException {
        try {
            this.writer = Cipher.getInstance(TRANSFORMATION);
            this.reader = Cipher.getInstance(TRANSFORMATION);
            this.keyWriter = Cipher.getInstance(KEY_TRANSFORMATION);
            this.setName(preferenceName);
            this.setEncrypt(encryptKeys);
        } catch (GeneralSecurityException e) {
            throw new SecurePreferencesException(e);
        }
    }

    public Cipher getWriter() {
        return writer;
    }

    public Cipher getReader() {
        return reader;
    }

    public Cipher getKeyWriter() {
        return keyWriter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEncrypt() {
        return encrypt;
    }

    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
    }

}