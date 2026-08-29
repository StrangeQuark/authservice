package com.strangequark.authservice.authorization;

import com.strangequark.authservice.user.Role;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class AuthorizationInitializer implements ApplicationRunner {
    private final AuthorizationRepository authorizationRepository;
    private final RoleAuthorizationRepository roleAuthorizationRepository;

    public AuthorizationInitializer(AuthorizationRepository authorizationRepository,
                                    RoleAuthorizationRepository roleAuthorizationRepository) {
        this.authorizationRepository = authorizationRepository;
        this.roleAuthorizationRepository = roleAuthorizationRepository;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) {
        Authorization authAuthorization = getOrCreateAuthorization("AUTH_API_ACCESS");
        Authorization emailAuthorization = getOrCreateAuthorization("EMAIL_API_ACCESS"); // Integration line: Email
        Authorization fileAuthorization = getOrCreateAuthorization("FILE_API_ACCESS"); // Integration line: File
        Authorization vaultAuthorization = getOrCreateAuthorization("VAULT_API_ACCESS"); // Integration line: Vault
        Authorization telemetryAuthorization = getOrCreateAuthorization("TELEMETRY_API_ACCESS"); // Integration line: Telemetry
        Authorization telemetryReadAuthorization = getOrCreateAuthorization("TELEMETRY_READ_ACCESS"); // Integration line: Telemetry

        addRoleAuthorization(Role.USER, authAuthorization);
        addRoleAuthorization(Role.USER, fileAuthorization); // Integration line: File
        addRoleAuthorization(Role.USER, vaultAuthorization); // Integration line: Vault

        addRoleAuthorization(Role.DEVELOPER, authAuthorization);
        addRoleAuthorization(Role.DEVELOPER, fileAuthorization); // Integration line: File
        addRoleAuthorization(Role.DEVELOPER, vaultAuthorization); // Integration line: Vault

        addRoleAuthorization(Role.ADMIN, authAuthorization);
        addRoleAuthorization(Role.ADMIN, fileAuthorization); // Integration line: File
        addRoleAuthorization(Role.ADMIN, vaultAuthorization); // Integration line: Vault

        addRoleAuthorization(Role.SUPER, authAuthorization);
        addRoleAuthorization(Role.SUPER, emailAuthorization); // Integration line: Email
        addRoleAuthorization(Role.SUPER, fileAuthorization); // Integration line: File
        addRoleAuthorization(Role.SUPER, vaultAuthorization); // Integration line: Vault
        addRoleAuthorization(Role.SUPER, telemetryReadAuthorization); // Integration line: Telemetry
    }

    private Authorization getOrCreateAuthorization(String name) {
        return authorizationRepository.findByName(name)
                .orElseGet(() -> authorizationRepository.save(new Authorization(name)));
    }

    private void addRoleAuthorization(Role role, Authorization authorization) {
        if(roleAuthorizationRepository.findByRoleAndAuthorization(role, authorization).isEmpty())
            roleAuthorizationRepository.save(new RoleAuthorization(role, authorization));
    }
}
