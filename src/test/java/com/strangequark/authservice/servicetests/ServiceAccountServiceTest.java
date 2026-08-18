package com.strangequark.authservice.servicetests;

import com.strangequark.authservice.config.JwtService;
import com.strangequark.authservice.serviceaccount.ServiceAccount;
import com.strangequark.authservice.serviceaccount.ServiceAccountRepository;
import com.strangequark.authservice.serviceaccount.ServiceAccountRequest;
import com.strangequark.authservice.serviceaccount.ServiceAccountService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

public class ServiceAccountServiceTest extends BaseServiceTest {

    @Autowired
    ServiceAccountService serviceAccountService;
    @Autowired
    ServiceAccountRepository serviceAccountRepository;
    @Autowired
    JwtService jwtService;

    private ServiceAccount testServiceAccount;
    private String accessToken;

    @BeforeEach
    void setup() {
        testServiceAccount = new ServiceAccount();
        testServiceAccount.setClientId("testClientId" + UUID.randomUUID());
        testServiceAccount.setClientPassword(passwordEncoder.encode("testClientPassword"));
        serviceAccountRepository.save(testServiceAccount);

        accessToken = jwtService.generateServiceAccountToken(testServiceAccount, false);

        //Set the accessToken to the Authorization header in the requestContextHolder
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + accessToken);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void authenticateTest() {
        ServiceAccountRequest request = new ServiceAccountRequest(testServiceAccount.getClientId(), "testClientPassword");

        ResponseEntity<?> response =  serviceAccountService.authenticate(request);

        Assertions.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void serviceAccountAuthoritiesTest() {
        Assertions.assertTrue(testServiceAccount.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("SERVICE_ACCOUNT")));

        Assertions.assertTrue(testServiceAccount.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(testServiceAccount.getClientId().toUpperCase() + "_SERVICE")));
    }
}
