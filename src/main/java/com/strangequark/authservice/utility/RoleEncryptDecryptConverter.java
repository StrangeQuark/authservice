package com.strangequark.authservice.utility;

import com.strangequark.authservice.user.Role;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Utility for automatically encrypting and decrypting {@link Role} fields in {@link com.strangequark.authservice.user.User} objects
 */
@Component
@Converter(autoApply = false)
public class RoleEncryptDecryptConverter implements AttributeConverter<Role, String> {

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
    public String convertToDatabaseColumn(Role role) {
        if (role == null) return null;
        return encryptionService.encrypt(role.name());
    }

    /**
     * Decrypt field when retrieving from database
     */
    @Override
    public Role convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        String decrypted = encryptionService.decrypt(dbData);
        return Role.valueOf(decrypted);
    }
}
