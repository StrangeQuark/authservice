package com.strangequark.authservice.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@link RestController} for user registration and authentication
 */
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    /**
     * {@link AuthenticationService}
     */
    private final AuthenticationService authenticationService;

    /**
     * Constructs a new {@code AuthenticationController} with the given dependencies.
     *
     * @param authenticationService {@link AuthenticationService} for handling authentication requests' business logic
     */
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Post request endpoint for registering a new user
     * @param request {@link RegistrationRequest}
     * @return {@link ResponseEntity}
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest request) {
        return authenticationService.register(request);
    }

    /**
     * Post request endpoint for authenticating a user
     * @param request {@link AuthenticationRequest}
     * @return {@link ResponseEntity}
     */
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest request) {
        return authenticationService.authenticate(request);
    }
}
