package com.strangequark.authservice.utility;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Utility for automatically encrypting and decrypting {@link String} fields in {@link com.strangequark.authservice.user.User} objects
 */
@Component
@Converter(autoApply = false)
public class EncryptDecryptConverter implements AttributeConverter<String, String> {

    /**
     * {@link EncryptionService} object for encrypting and decrypting fields
     */
    private static EncryptionService encryptionService;

    /**
     * Injected {@link EncryptionService} object for encrypting and decrypting fields
     */
    @Autowired
    private EncryptionService injectedEncryptionService;

    /**
     * Initialize encryptionService to injectedEncryptionService, JPA requires encryptionService to be a static field
     */
    @PostConstruct
    public void init() {
        encryptionService = injectedEncryptionService;
    }

    /**
     * Encrypt field when inserting into database
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute == null ? null : encryptionService.encrypt(attribute);
    }

    /**
     * Decrypt field when retrieving from database
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData == null ? null : encryptionService.decrypt(dbData);
    }
}
