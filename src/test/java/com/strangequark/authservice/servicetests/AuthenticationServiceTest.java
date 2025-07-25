package com.strangequark.authservice.servicetests;

import com.strangequark.authservice.auth.AuthenticationRequest;
import com.strangequark.authservice.auth.AuthenticationService;
import com.strangequark.authservice.auth.RegistrationRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

public class AuthenticationServiceTest extends BaseServiceTest{
    @Autowired
    private AuthenticationService authenticationService;

    @Test
    void registerTest() {
        RegistrationRequest request = new RegistrationRequest("registerTestUser", "registerTest@test.com", "registerPassword");

        ResponseEntity<?> response =  authenticationService.register(request);

        Assertions.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void authenticateTest() {
        AuthenticationRequest request = new AuthenticationRequest("testUser", "password");

        ResponseEntity<?> response =  authenticationService.authenticate(request);

        Assertions.assertEquals(200, response.getStatusCode().value());
    }
}
