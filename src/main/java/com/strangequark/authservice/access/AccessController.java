package com.strangequark.authservice.access;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * {@link RestController} for issuing Access Tokens
 */
@RestController
@RequestMapping("/auth/access")
public class AccessController {
    /**
     * {@link AccessService}
     */
    private final AccessService accessService;

    public AccessController(AccessService accessService) {
        this.accessService = accessService;
    }

    /**
     * Get request endpoint for retrieving a new access key
     * @return {@link ResponseEntity}
     */
    @GetMapping()
    public ResponseEntity<?> serveAccessToken() {
        return accessService.serveAccessToken();
    }
}
