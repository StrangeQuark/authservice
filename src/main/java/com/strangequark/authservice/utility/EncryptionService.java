package com.strangequark.authservice.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Utility service for automatically encrypting and decrypting fields in {@link com.strangequark.authservice.user.User} objects
 */
@Component
public class EncryptionService {
    /**
     * {@link Logger} for writing {@link EncryptionService} application logs
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptionService.class);

    /**
     * Encryption algorithm
     */
    private final String ALGORITHM = "AES";

    /**
     * Secret key used for encryption/decryption
     */
    @Value("${ENCRYPTION_KEY}")
    private String ENCRYPTION_KEY;

    /**
     * Utility method for automatically encrypting data
     */
    public String encrypt(String data) {
        try {
            LOGGER.info("Attempting to encrypt data");

            SecretKey key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), ALGORITHM);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            LOGGER.info("Data successfully encrypted");
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
        } catch (Exception e) {
            LOGGER.error("Encryption error");
            throw new RuntimeException("Encryption error", e);
        }
    }

    /**
     * Utility method for automatically decrypting data
     */
    public String decrypt(String data) {
        try {
            LOGGER.info("Attempting to decrypt data");
            SecretKey key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), ALGORITHM);

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);

            LOGGER.info("Data successfully decrypted");
            return new String(cipher.doFinal(Base64.getDecoder().decode(data)));
        } catch (Exception e) {
            LOGGER.error("Decryption error");
            throw new RuntimeException("Decryption error", e);
        }
    }
}
