package com.strangequark.authservice.servicetests;

import com.strangequark.authservice.user.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class UserServiceTest extends BaseServiceTest{

    @Autowired
    private UserService userService;

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
}
