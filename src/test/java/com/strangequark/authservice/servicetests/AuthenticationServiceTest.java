package com.strangequark.authservice.servicetests;

import com.strangequark.authservice.auth.AuthenticationRequest;
import com.strangequark.authservice.auth.AuthenticationService;
import com.strangequark.authservice.auth.RegistrationRequest;
import com.strangequark.authservice.utility.EmailType;
import com.strangequark.authservice.utility.EmailUtility;
import com.strangequark.authservice.user.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class AuthenticationServiceTest extends BaseServiceTest{
    @Autowired
    private AuthenticationService authenticationService;
    @MockitoBean
    private EmailUtility emailUtility;

    @BeforeEach
    void setupEmailUtility() {
        ReflectionTestUtils.setField(authenticationService, "INVITE_ONLY", false);
        when(emailUtility.sendEmail(anyString(), eq(EmailType.REGISTER))).thenReturn(ResponseEntity.ok().build());
    }

    @Test
    void registerTest() {
        RegistrationRequest request = new RegistrationRequest("registerTestUser", "registerTest@test.com", "registerPassword");

        ResponseEntity<?> response =  authenticationService.register(request);

        Assertions.assertEquals(200, response.getStatusCode().value());
        User user = userRepository.findByUsername(request.getUsername()).get();
        Assertions.assertFalse(user.isEnabled());
    }

    @Test
    void registerWhenEmailServiceFailsTest() {
        when(emailUtility.sendEmail(anyString(), eq(EmailType.REGISTER)))
                .thenThrow(new ResourceAccessException("Email service unavailable"));

        RegistrationRequest request = new RegistrationRequest("failedRegisterUser", "failedRegister@test.com", "registerPassword");

        ResponseEntity<?> response = authenticationService.register(request);

        Assertions.assertEquals(503, response.getStatusCode().value());
        Assertions.assertTrue(userRepository.findByUsername(request.getUsername()).isEmpty());
    }

    @Test
    void authenticateTest() {
        AuthenticationRequest request = new AuthenticationRequest("testUser", "password");

        ResponseEntity<?> response =  authenticationService.authenticate(request);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertNotNull(response.getHeaders().getFirst("Set-Cookie"));
        Assertions.assertTrue(response.getHeaders().getFirst("Set-Cookie").contains("refresh_token="));
        Assertions.assertTrue(response.getHeaders().getFirst("Set-Cookie").contains("HttpOnly"));
    }
}
