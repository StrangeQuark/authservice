package com.strangequark.authservice.utility;

import com.strangequark.authservice.user.Role;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Converter(autoApply = false)
public class RoleEncryptDecryptConverter implements AttributeConverter<Role, String> {

    private static EncryptionService encryptionService;

    @Autowired
    private EncryptionService injectedEncryptionService;

    @PostConstruct
    public void init() {
        encryptionService = injectedEncryptionService;
    }

    @Override
    public String convertToDatabaseColumn(Role role) {
        if (role == null) return null;
        return encryptionService.encrypt(role.name());
    }

    @Override
    public Role convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        String decrypted = encryptionService.decrypt(dbData);
        return Role.valueOf(decrypted);
    }
}
