package com.strangequark.authservice.access;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * {@link RestController} for issuing Access Tokens
 */
@RestController
@RequestMapping("/api/auth/access")
public class AccessController {
    /**
     * {@link AccessService}
     */
    private final AccessService accessService;

    /**
     * Constructs a new {@code AccessController} with the given dependencies.
     *
     * @param accessService {@link AccessService} for handling Access request business logic
     */
    public AccessController(AccessService accessService) {
        this.accessService = accessService;
    }

    /**
     * Post request endpoint for retrieving a new access key
     * @return {@link ResponseEntity}
     */
    @PostMapping()
    public ResponseEntity<?> serveAccessToken() {
        return accessService.serveAccessToken();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return accessService.logout();
    }
}
