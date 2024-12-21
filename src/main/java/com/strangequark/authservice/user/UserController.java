package com.strangequark.authservice.user;

import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * {@link RestController} for manipulating {@link User} objects
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    /**
     * {@link UserService} for executing business logic on User objects
     */
    private final UserService userService;

    /**
     * Post request endpoint for updating a user's password
     * @param request {@link UpdatePasswordRequest}
     * @return {@link ResponseEntity}
     */
    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestBody UpdatePasswordRequest request) {
        return userService.updatePassword(request);
    }

    /**
     * Post request endpoint for adding to a user's set of authorities
     * @param {@link List} of strings of authorities to be added to the user
     * @return {@link ResponseEntity}
     */
    @PostMapping("/add-authorizations")
    public ResponseEntity<?> addAuthorizations(@RequestBody Set<String> authorizations) {
        return userService.addAuthorizations(authorizations);
    }

    /**
     * Post request endpoint for removing from a user's set of authorities
     * @param {@link List} of strings of authorities to be removed from the user
     * @return {@link ResponseEntity}
     */
    @PostMapping("/remove-authorizations")
    public ResponseEntity<?> removeAuthorizations(@RequestBody Set<String> authorizations) {
        return userService.removeAuthorizations(authorizations);
    }

    /**
     * Post request endpoint for initiating password resets
     * @param {@link List} of strings of authorities to be removed from the user
     * @return {@link ResponseEntity}
     */
    @PostMapping("/verify-user-and-send-email")
    public ResponseEntity<?> verifyUserAndSendPasswordResetEmail(@RequestBody UpdatePasswordRequest request) {
        return userService.verifyUserAndSendPasswordResetEmail(request);
    }

    /**
     * Get request for enabling a user
     * @param {@link Map} containing the email address of the user to enable
     * @return {@link ResponseEntity}
     */
    @PostMapping("/enableUser")
    public ResponseEntity<?> enableUser(@RequestBody Map<String, String> requestBody) {
        return userService.enableUser(requestBody);
    }
}
