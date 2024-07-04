package com.strangequark.authservice.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * {@link RestController} for demoing the application
 */
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    /**
     * {@link UserService}
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
}
