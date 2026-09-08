package com.strangequark.authservice.authorization;

public enum AuthorizationType {
    AUTH_API_ACCESS,
    EMAIL_API_ACCESS, // Integration line: Email
    FILE_API_ACCESS, // Integration line: File
    VAULT_API_ACCESS, // Integration line: Vault
    TELEMETRY_API_ACCESS, // Integration line: Telemetry
    TELEMETRY_READ_ACCESS, // Integration line: Telemetry
    INVITATION_MANAGEMENT,
    USER_MANAGEMENT,
    AUTHORIZATION_MANAGEMENT,
    EMAIL_TEMPLATE_MANAGEMENT // Integration line: Email
}
