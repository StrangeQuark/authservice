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
        for(AuthorizationType authorizationType : AuthorizationType.values())
            getOrCreateAuthorization(authorizationType);

        addRoleAuthorization(Role.USER, AuthorizationType.AUTH_API_ACCESS);
        addRoleAuthorization(Role.USER, AuthorizationType.FILE_API_ACCESS);

        addRoleAuthorization(Role.DEVELOPER, AuthorizationType.AUTH_API_ACCESS);
        addRoleAuthorization(Role.DEVELOPER, AuthorizationType.FILE_API_ACCESS);
        addRoleAuthorization(Role.DEVELOPER, AuthorizationType.VAULT_API_ACCESS);

        addRoleAuthorization(Role.ADMIN, AuthorizationType.AUTH_API_ACCESS);
        addRoleAuthorization(Role.ADMIN, AuthorizationType.FILE_API_ACCESS);
        addRoleAuthorization(Role.ADMIN, AuthorizationType.VAULT_API_ACCESS);

        for(AuthorizationType authorizationType : AuthorizationType.values())
            addRoleAuthorization(Role.SUPER, authorizationType);
    }

    private Authorization getOrCreateAuthorization(AuthorizationType authorizationType) {
        return authorizationRepository.findByName(authorizationType.name())
                .orElseGet(() -> authorizationRepository.save(new Authorization(authorizationType.name())));
    }

    private void addRoleAuthorization(Role role, AuthorizationType authorizationType) {
        addRoleAuthorization(role, getOrCreateAuthorization(authorizationType));
    }

    private void addRoleAuthorization(Role role, Authorization authorization) {
        if(roleAuthorizationRepository.findByRoleAndAuthorization(role, authorization).isEmpty())
            roleAuthorizationRepository.save(new RoleAuthorization(role, authorization));
    }
}
