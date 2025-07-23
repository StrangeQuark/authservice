package com.strangequark.authservice.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * {@link RestController} for manipulating {@link User} objects
 */
@RestController
@RequestMapping("/api/auth/user")
public class UserController {
    /**
     * {@link UserService} for executing business logic on User objects
     */
    private final UserService userService;

    /**
     * Constructs a new {@code UserController} with the given dependencies.
     *
     * @param userService {@link UserService} for performing business logic on {@link User} object requests
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Post request endpoint for updating a user's password
     * @param request {@link UserRequest}
     * @return {@link ResponseEntity}
     */
    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestBody UserRequest request) {
        return userService.updatePassword(request);
    }

    /**
     * Post request endpoint for adding to a user's set of authorities
     * @param {@link List} of strings of authorities to be added to the user
     * @return {@link ResponseEntity}
     */
    @PostMapping("/add-authorizations-to-user")
    public ResponseEntity<?> addAuthorizationsToUser(@RequestBody UserRequest request) {
        return userService.addAuthorizationsToUser(request);
    }

    /**
     * Post request endpoint for removing from a user's set of authorities
     * @param {@link List} of strings of authorities to be removed from the user
     * @return {@link ResponseEntity}
     */
    @PostMapping("/remove-authorizations")
    public ResponseEntity<?> removeAuthorizations(@RequestBody UserRequest request) {
        return userService.removeAuthorizations(request);
    }

    // Integration function start: Email
    /**
     * Post request endpoint for initiating password resets
     * @param {@link UserRequest} containing user credentials
     * @return {@link ResponseEntity}
     */
    @PostMapping("/send-password-reset-email")
    public ResponseEntity<?> sendPasswordResetEmail(@RequestBody UserRequest request) {
        return userService.sendPasswordResetEmail(request);
    } // Integration function end: Email

    /**
     * Post request endpoint for enabling a user
     * @param {@link Map} containing the email address of the user to enable
     * @return {@link ResponseEntity}
     */
    @PostMapping("/enable-user")
    public ResponseEntity<?> enableUser(@RequestBody Map<String, String> requestBody) {
        return userService.enableUser(requestBody);
    }

    /**
     * Post request endpoint for deleting a user
     * @param {@link UserRequest} containing the user's credentials
     * @return {@link ResponseEntity}
     */
    @PostMapping("/delete-user")
    public ResponseEntity<?> deleteUser(@RequestBody UserRequest userRequest) {
        return userService.deleteUser(userRequest);
    }

    /**
     * Post request endpoint for updating a user's email
     * @param {@link UserRequest} containing the user's credentials
     * @return {@link ResponseEntity}
     */
    @PostMapping("/update-email")
    public ResponseEntity<?> updateEmail(@RequestBody UserRequest userRequest) {
        return userService.updateEmail(userRequest);
    }

    /**
     * Post request endpoint for updating a user's username
     * @param {@link UserRequest} containing the user's credentials
     * @return {@link ResponseEntity}
     */
    @PostMapping("/update-username")
    public ResponseEntity<?> updateUsername(@RequestBody UserRequest userRequest) {
        return userService.updateUsername(userRequest);
    }

    /**
     * Get request endpoint for retrieving a user's ID from their username
     * @param {@link UserRequest} containing the user's credentials
     * @return {@link ResponseEntity}
     */
    @GetMapping("/get-user-id")
    public ResponseEntity<?> getUserId(@RequestParam String username) {
        return userService.getUserId(username);
    }
}
