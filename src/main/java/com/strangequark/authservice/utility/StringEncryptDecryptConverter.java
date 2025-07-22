package com.strangequark.authservice.utility;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Utility for automatically encrypting and decrypting {@link String} fields in {@link com.strangequark.authservice.user.User} objects
 */
@Converter(autoApply = false)
public class StringEncryptDecryptConverter implements AttributeConverter<String, String> {

    /**
     * Encrypt field when inserting into database
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute == null ? null : EncryptionUtility.encrypt(attribute);
    }

    /**
     * Decrypt field when retrieving from database
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData == null ? null : EncryptionUtility.decrypt(dbData);
    }
}
