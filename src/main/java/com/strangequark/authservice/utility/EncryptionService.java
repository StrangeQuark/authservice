package com.strangequark.authservice.utility;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
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
     * Secret key used for encryption/decryption
     */
    private final String secretKey;

    /**
     * {@link SecretKeySpec} used for encryption/decryption
     */
    private SecretKeySpec keySpec;

    /**
     * Constructs a new {@code AccessService} with the given dependencies.
     *
     * @param secretKey Secret key used for encryption/decryption
     */
    public EncryptionService(@Value("${encryption.key}") String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * Initialize the keySpec object using the secretKey and AES algorithm
     */
    @PostConstruct
    public void init() {
        keySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
    }

    /**
     * Utility method for automatically encrypting data
     */
    public String encrypt(String data) {
        try {
            LOGGER.info("Attempting to encrypt data");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            LOGGER.info("Data successfully encrypted");
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
        } catch (Exception ex) {
            LOGGER.error("Encryption error");
            throw new RuntimeException("Encryption error", ex);
        }
    }

    /**
     * Utility method for automatically decrypting data
     */
    public String decrypt(String data) {
        try {
            LOGGER.info("Attempting to decrypt data");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            LOGGER.info("Data successfully decrypted");
            return new String(cipher.doFinal(Base64.getDecoder().decode(data)));
        } catch (Exception ex) {
            LOGGER.error("Decryption error");
            throw new RuntimeException("Decryption error", ex);
        }
    }
}
