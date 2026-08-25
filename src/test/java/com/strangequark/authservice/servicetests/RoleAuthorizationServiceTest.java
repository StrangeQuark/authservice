package com.strangequark.authservice.servicetests;

import com.strangequark.authservice.authorization.Authorization;
import com.strangequark.authservice.authorization.RoleAuthorizationRequest;
import com.strangequark.authservice.authorization.RoleAuthorizationService;
import com.strangequark.authservice.user.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public class RoleAuthorizationServiceTest extends BaseServiceTest {
    @Autowired
    private RoleAuthorizationService roleAuthorizationService;

    @Test
    void addRoleAuthorizationTest() {
        String authorizationName = "TEST_AUTHORIZATION_" + UUID.randomUUID();
        authorizationRepository.save(new Authorization(authorizationName));
        RoleAuthorizationRequest request = new RoleAuthorizationRequest();
        request.setRole(Role.ADMIN);
        request.setAuthorization(authorizationName);

        ResponseEntity<?> response = roleAuthorizationService.addRoleAuthorization(request);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Authorization authorization = authorizationRepository.findByName(authorizationName).get();
        Assertions.assertTrue(roleAuthorizationRepository.findByRoleAndAuthorization(Role.ADMIN, authorization).isPresent());
    }

    @Test
    void removeRoleAuthorizationTest() {
        String authorizationName = "TEST_AUTHORIZATION_" + UUID.randomUUID();
        authorizationRepository.save(new Authorization(authorizationName));
        RoleAuthorizationRequest request = new RoleAuthorizationRequest();
        request.setRole(Role.ADMIN);
        request.setAuthorization(authorizationName);
        roleAuthorizationService.addRoleAuthorization(request);

        ResponseEntity<?> response = roleAuthorizationService.removeRoleAuthorization(request);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Authorization authorization = authorizationRepository.findByName(authorizationName).get();
        Assertions.assertTrue(roleAuthorizationRepository.findByRoleAndAuthorization(Role.ADMIN, authorization).isEmpty());
    }
}
