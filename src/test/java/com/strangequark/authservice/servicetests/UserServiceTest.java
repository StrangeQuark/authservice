package com.strangequark.authservice.servicetests;

import com.strangequark.authservice.error.ErrorResponse; // Integration line: Email
import com.strangequark.authservice.user.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.*;

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

    @Test // Integration function start: Email
    void sendPasswordResetEmailTest() {
        UserRequest userRequest = new UserRequest();
        userRequest.setEmail(testUser.getEmail());

        ResponseEntity<?> response =  userService.sendPasswordResetEmail(userRequest);

        Assertions.assertEquals(500, response.getStatusCode().value());
        Assertions.assertEquals("Unable to send password reset email", ((ErrorResponse) response.getBody()).getErrorMessage());
    }

    @Test
    void resetPasswordTest() {
        setupEmailServiceAccount();

        UserRequest userRequest = new UserRequest();
        userRequest.setEmail(testUser.getEmail());
        userRequest.setNewPassword("newPassword");

        ResponseEntity<?> response =  userService.resetPassword(userRequest);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("Password reset success", ((UserResponse) response.getBody()).getMessage());
    }
    // Integration function end: Email
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
        userRequest.setNewEmail(newEmail);
        userRequest.setPassword("password");

        ResponseEntity<?> response =  userService.updateEmail(userRequest);

        User user = userRepository.findByUsername(testUser.getUsername()).get();

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("Email successfully updated", ((UserResponse) response.getBody()).getMessage());
        Assertions.assertEquals(newEmail, userRepository.findByUsername(testUser.getUsername()).get().getEmail());
    }

    @Test
    void updateUsernameTest() {
        String newUsername = "newUsername";

        UserRequest userRequest = new UserRequest();
        userRequest.setNewUsername(newUsername);
        userRequest.setPassword("password");

        ResponseEntity<?> response =  userService.updateUsername(userRequest);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertTrue(userRepository.findByUsername(newUsername).isPresent());
        Assertions.assertFalse(userRepository.findByUsername(testUser.getUsername()).isPresent());
    }

    @Test
    void updateRoleTest() {
        Role newRole = Role.SUPER;

        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(testUser.getUsername());
        userRequest.setNewRole(newRole);

        // Switch to Admin User
        setupAdminUser();

        ResponseEntity<?> response =  userService.updateRole(userRequest);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertTrue(userRepository.findByUsername(testUser.getUsername()).isPresent());
        Assertions.assertEquals(Role.SUPER, userRepository.findByUsername(testUser.getUsername()).get().getRole());
    }

    @Test
    void getUserIdTest() {
        ResponseEntity<?> response = userService.getUserId(testUser.getUsername());

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals(testUser.getId(), response.getBody());
    }

    @Test
    void searchUsersTest() {
        ResponseEntity<?> response = userService.searchUsers(testUser.getUsername());

        UserResponse userResponse = (UserResponse) response.getBody();

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals(testUser.getId(), userResponse.getUserId());
        Assertions.assertEquals(testUser.getUsername(), userResponse.getUsername());
        Assertions.assertEquals(testUser.getEmail(), userResponse.getEmail());
    }

    @Test
    void getUserDetailsByIdsTest() {
        List<UUID> ids = new ArrayList<>();

        ids.add(testUser.getId());
        ids.add(UUID.randomUUID());

        ResponseEntity<?> response = userService.getUserDetailsByIds(ids);
        List<UserResponse> userResponseList = (List<UserResponse>) response.getBody();

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals(1, userResponseList.size());
        Assertions.assertEquals(testUser.getUsername(), userResponseList.getFirst().getUsername());
    }
}
