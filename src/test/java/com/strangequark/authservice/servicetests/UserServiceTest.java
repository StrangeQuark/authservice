package com.strangequark.authservice.servicetests;

import com.strangequark.authservice.config.JwtService;
import com.strangequark.authservice.user.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class UserServiceTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

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
    void updatePasswordTest() {
        UpdatePasswordRequest updatePasswordRequest = new UpdatePasswordRequest(testUser.getUsername(), "password", "newPassword");

        ResponseEntity<?> response =  userService.updatePassword(updatePasswordRequest);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("Password was successfully reset", ((UserResponse) response.getBody()).getMessage());
    }

    @Test
    void addAuthorizationsTest() {
        Set<String> authorizations = new HashSet<>();
        authorizations.add("Auth 1");
        authorizations.add("test 2");

        ResponseEntity<?> response =  userService.addAuthorizations(authorizations);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("Authorizations were successfully added", ((UserResponse) response.getBody()).getMessage());
    }

    @Test
    void removeAuthorizationsTest() {
        Set<String> authorzationsToRemove = new HashSet<>();
        authorzationsToRemove.add("testAuthorization1");

        ResponseEntity<?> response =  userService.removeAuthorizations(authorzationsToRemove);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("Authorizations were successfully removed", ((UserResponse) response.getBody()).getMessage());
    }

    @Test
    void enableUserTest() {
        User disabledTestUser = new User("disabledTestUser", "disabledTest@test.com", Role.USER, false, new HashSet<>(), passwordEncoder.encode("password"));
        userRepository.save(disabledTestUser);

        Map<String, String> request = new HashMap<>();
        request.put("email", "disabledTest@test.com");

        ResponseEntity<?> response =  userService.enableUser(request);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("User is enabled", ((UserResponse) response.getBody()).getMessage());
    }

    @AfterEach
    void teardown() {
        try {
            userRepository.delete(testUser);
            accessToken = null;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
