package com.strangequark.authservice.authorization;

import com.strangequark.authservice.user.Role;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Order(0)
public class AuthorizationInitializer implements ApplicationRunner {
    private final AuthorizationRepository authorizationRepository;
    private final RoleAuthorizationRepository roleAuthorizationRepository;

    @Value("${initial.authorizations}")
    private String initialAuthorizations;

    @Value("${initial.user.authorizations}")
    private String initialUserAuthorizations;

    @Value("${initial.developer.authorizations}")
    private String initialDeveloperAuthorizations;

    @Value("${initial.admin.authorizations}")
    private String initialAdminAuthorizations;

    @Value("${initial.super.authorizations}")
    private String initialSuperAuthorizations;

    public AuthorizationInitializer(AuthorizationRepository authorizationRepository,
                                    RoleAuthorizationRepository roleAuthorizationRepository) {
        this.authorizationRepository = authorizationRepository;
        this.roleAuthorizationRepository = roleAuthorizationRepository;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) {
        for(String authorizationName : initialAuthorizations.split(","))
            getOrCreateAuthorization(authorizationName.trim());

        addRoleAuthorizations(Role.USER, initialUserAuthorizations);
        addRoleAuthorizations(Role.DEVELOPER, initialDeveloperAuthorizations);
        addRoleAuthorizations(Role.ADMIN, initialAdminAuthorizations);
        addRoleAuthorizations(Role.SUPER, initialSuperAuthorizations);
    }

    private Authorization getOrCreateAuthorization(String name) {
        if(name.equals("ALL"))
            throw new RuntimeException("ALL is reserved for initial role authorizations");

        return authorizationRepository.findByName(name)
                .orElseGet(() -> authorizationRepository.save(new Authorization(name)));
    }

    private void addRoleAuthorizations(Role role, String authorizationNames) {
        if(Arrays.stream(authorizationNames.split(",")).map(String::trim).anyMatch("ALL"::equals)) {
            for(Authorization authorization : authorizationRepository.findAll())
                addRoleAuthorization(role, authorization);

            return;
        }

        for(String authorizationName : authorizationNames.split(","))
            addRoleAuthorization(role, getOrCreateAuthorization(authorizationName.trim()));
    }

    private void addRoleAuthorization(Role role, Authorization authorization) {
        if(roleAuthorizationRepository.findByRoleAndAuthorization(role, authorization).isEmpty())
            roleAuthorizationRepository.save(new RoleAuthorization(role, authorization));
    }
}
