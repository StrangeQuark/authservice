package com.strangequark.authservice.utility;

import com.strangequark.authservice.user.Role;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Utility for automatically encrypting and decrypting {@link Role} fields in {@link com.strangequark.authservice.user.User} objects
 */
@Converter(autoApply = false)
public class RoleEncryptDecryptConverter implements AttributeConverter<Role, String> {

    /**
     * Encrypt field when inserting into database
     */
    @Override
    public String convertToDatabaseColumn(Role role) {
        if (role == null) return null;
        return EncryptionUtility.encrypt(role.name());
    }

    /**
     * Decrypt field when retrieving from database
     */
    @Override
    public Role convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        String decrypted = EncryptionUtility.decrypt(dbData);
        return Role.valueOf(decrypted);
    }
}
