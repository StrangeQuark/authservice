package com.strangequark.userservice.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@link RestController} for user registration and authentication
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    /**
     * {@link AuthenticationService} for registering and authenticating users
     */
    private final AuthenticationService authenticationService;

    /**
     * Post request endpoint for registering a new user
     * @param request {@link RegistrationRequest} containing
     * @return {@link ResponseEntity}
     */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegistrationRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    /**
     * Post request endpoint for authenticating a user
     * @param request {@link AuthenticationRequest} containing
     * @return {@link ResponseEntity}
     */
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
}
