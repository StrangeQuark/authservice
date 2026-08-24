package com.strangequark.authservice.servicetests;

import com.strangequark.authservice.authorization.AuthorizationRequest;
import com.strangequark.authservice.authorization.AuthorizationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public class AuthorizationServiceTest extends BaseServiceTest {
    @Autowired
    private AuthorizationService authorizationService;

    @Test
    void createAuthorizationTest() {
        AuthorizationRequest request = new AuthorizationRequest();
        String authorizationName = "TEST_AUTHORIZATION_" + UUID.randomUUID();
        request.setName(authorizationName);

        ResponseEntity<?> response = authorizationService.createAuthorization(request);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertTrue(authorizationRepository.findByName(authorizationName).isPresent());
    }

    @Test
    void deleteAuthorizationTest() {
        AuthorizationRequest request = new AuthorizationRequest();
        String authorizationName = "TEST_AUTHORIZATION_" + UUID.randomUUID();
        request.setName(authorizationName);
        authorizationService.createAuthorization(request);

        ResponseEntity<?> response = authorizationService.deleteAuthorization(authorizationName);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertTrue(authorizationRepository.findByName(authorizationName).isEmpty());
    }
}
