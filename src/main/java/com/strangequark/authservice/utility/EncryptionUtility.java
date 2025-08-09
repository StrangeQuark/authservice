package com.strangequark.authservice.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Utility service for automatically encrypting and decrypting fields in {@link com.strangequark.authservice.user.User} objects
 */
public class EncryptionUtility {
    /**
     * {@link Logger} for writing {@link EncryptionUtility} application logs
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptionUtility.class);

    /**
     * Encryption algorithm
     */
    private static final String ALGORITHM = "AES";

    /**
     * Secret key used for encryption/decryption
     */
    private static final String ENCRYPTION_KEY = resolveKey();

    /**
     * Utility configuration method for getting the encryption key from properties/env vars
     */
    private static String resolveKey() {
        LOGGER.info("Attempting to resolve encryption key");

        String key = System.getProperty("ENCRYPTION_KEY");
        if (key == null) {
            LOGGER.info("Unable to grab from properties, attempt with environment variables");
            key = System.getenv("ENCRYPTION_KEY");
        }
        if (key == null || key.length() != 32) {
            LOGGER.error("ENCRYPTION_KEY must be set and 32 chars long");
            throw new IllegalStateException("ENCRYPTION_KEY must be set and 32 chars long");
        }

        LOGGER.info("Encryption key successfully resolved");
        return key;
    }

    /**
     * Utility method for automatically encrypting data
     */
    public static String encrypt(String data) {
        try {
            SecretKey key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), ALGORITHM);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
    }

    /**
     * Utility method for automatically decrypting data
     */
    public static String decrypt(String data) {
        try {
            SecretKey key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), ALGORITHM);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);

            return new String(cipher.doFinal(Base64.getDecoder().decode(data)));
        } catch (Exception e) {
            throw new RuntimeException("Decryption error", e);
        }
    }
}
