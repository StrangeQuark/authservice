package com.strangequark.authservice.servicetests;

import com.strangequark.authservice.error.ErrorResponse;
import com.strangequark.authservice.user.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;
import java.util.Set;

public class UserServiceTest extends BaseServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void updatePasswordTest() {
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(testUser.getUsername());
        userRequest.setPassword("password");
        userRequest.setNewPassword("newPassword");

        ResponseEntity<?> response =  userService.updatePassword(userRequest);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("Password successfully updated", ((UserResponse) response.getBody()).getMessage());
    }

    @Test
    void addAuthorizationsToUserTest() {
        //Init the Admin user as the request context holder
        setupAdminUser();

        Set<String> authorizations = new HashSet<>();
        authorizations.add("Auth 1");
        authorizations.add("test 2");

        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(testUser.getUsername());
        userRequest.setAuthorizations(authorizations);

        ResponseEntity<?> response =  userService.addAuthorizationsToUser(userRequest);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("Authorizations successfully added", ((UserResponse) response.getBody()).getMessage());
    }

    @Test
    void removeAuthorizationsTest() {
        Set<String> authorizationsToRemove = new HashSet<>();
        authorizationsToRemove.add("testAuthorization1");

        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(testUser.getUsername());
        userRequest.setAuthorizations(authorizationsToRemove);

        ResponseEntity<?> response =  userService.removeAuthorizations(userRequest);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("Authorizations successfully removed", ((UserResponse) response.getBody()).getMessage());
    }

    @Test
    void sendPasswordResetEmailTest() {
        UserRequest userRequest = new UserRequest();
        userRequest.setEmail(testUser.getEmail());

        ResponseEntity<?> response =  userService.sendPasswordResetEmail(userRequest);

        Assertions.assertEquals(500, response.getStatusCode().value());
        Assertions.assertEquals("Unable to send password reset email", ((ErrorResponse) response.getBody()).getErrorMessage());
    }

    @Test
    void enableUserTest() {
        User disabledTestUser = new User("disabledTestUser", "disabledTest@test.com", Role.USER, false, new HashSet<>(), passwordEncoder.encode("password"));
        userRepository.save(disabledTestUser);

        UserRequest userRequest = new UserRequest();
        userRequest.setEmail(disabledTestUser.getEmail());

        ResponseEntity<?> response =  userService.enableUser(userRequest);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("User has been enabled", ((UserResponse) response.getBody()).getMessage());
    }

    @Test
    void disableUserTest() {
        //Init the Admin user as the request context holder
        setupAdminUser();

        User enabledTestUser = new User("enabledTestUser", "enabledTest@test.com", Role.USER, true, new HashSet<>(), passwordEncoder.encode("password"));
        userRepository.save(enabledTestUser);

        UserRequest userRequest = new UserRequest();
        userRequest.setEmail(enabledTestUser.getEmail());

        ResponseEntity<?> response =  userService.disableUser(userRequest);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("User has been disabled", ((UserResponse) response.getBody()).getMessage());
    }

    @Test
    void deleteUserTest() {
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(testUser.getUsername());
        userRequest.setPassword("password");

        ResponseEntity<?> response =  userService.deleteUser(userRequest);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("User successfully deleted", ((UserResponse) response.getBody()).getMessage());
        Assertions.assertFalse(userRepository.findByUsername(testUser.getUsername()).isPresent());
    }

    @Test
    void updateEmailTest() {
        String newEmail = "new@test.com";

        UserRequest userRequest = new UserRequest();
        userRequest.setEmail(newEmail);
        userRequest.setPassword("password");

        ResponseEntity<?> response =  userService.updateEmail(userRequest);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("Email successfully updated", ((UserResponse) response.getBody()).getMessage());
        Assertions.assertEquals(userRepository.findByUsername(testUser.getUsername()).get().getEmail(), newEmail);
    }

    @Test
    void updateUsernameTest() {
        String newUsername = "newUsername";

        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(newUsername);
        userRequest.setPassword("password");

        ResponseEntity<?> response =  userService.updateUsername(userRequest);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertTrue(userRepository.findByUsername(newUsername).isPresent());
        Assertions.assertFalse(userRepository.findByUsername(testUser.getUsername()).isPresent());
    }

    @Test
    void getUserIdTest() {
        ResponseEntity<?> response = userService.getUserId(testUser.getUsername());

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals(testUser.getId(), response.getBody());
    }
}
