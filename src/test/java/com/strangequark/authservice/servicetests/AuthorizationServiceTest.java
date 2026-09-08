package com.strangequark.authservice.servicetests;

import com.strangequark.authservice.authorization.AuthorizationService;
import com.strangequark.authservice.authorization.Authorization;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class AuthorizationServiceTest extends BaseServiceTest {
    @Autowired
    private AuthorizationService authorizationService;

    @Test
    void getAuthorizationsTest() {
        ResponseEntity<?> response = authorizationService.getAuthorizations();
        List<Authorization> authorizations = (List<Authorization>) response.getBody();

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertTrue(authorizations.stream()
                .anyMatch(authorization -> authorization.getName().equals("AUTH_API_ACCESS")));
    }
}
