package com.strangequark.authservice.servicetests;

import com.strangequark.authservice.auth.AuthenticationRequest;
import com.strangequark.authservice.auth.AuthenticationService;
import com.strangequark.authservice.config.JwtService;
import com.strangequark.authservice.user.Role;
import com.strangequark.authservice.user.User;
import com.strangequark.authservice.user.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashSet;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class AuthenticationServiceTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationService authenticationService;
    private User testUser;
    private String accessToken;

    @BeforeEach
    void setup() {
        HashSet<String> testAuthorizations = new HashSet<>();
        testAuthorizations.add("testAuthorization1");

        testUser = new User("testUser", "test@test.com", Role.USER, true, testAuthorizations, passwordEncoder.encode("password"));
        userRepository.save(testUser);

        accessToken = jwtService.generateToken(testUser, false);

        //Set the accessToken to the Authorization header in the requestContextHolder
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + accessToken);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void authenticateTest() {
        AuthenticationRequest request = new AuthenticationRequest("testUser", "password");

        ResponseEntity<?> response =  authenticationService.authenticate(request);

        Assertions.assertEquals(200, response.getStatusCode().value());
    }

    @AfterEach
    void teardown() {
        userRepository.deleteAll();
        accessToken = null;
        testUser = null;
    }
}
