package com.strangequark.authservice.access;

import com.strangequark.authservice.auth.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * {@link RestController} for demoing the application
 */
@RestController
@RequestMapping("/api/v1/access")
@RequiredArgsConstructor
public class AccessController {
    /**
     * {@link AuthenticationService} for registering and authenticating users
     */
    private final AccessService accessService;

    /**
     * Get request endpoint for retrieving a new access key
     * @return {@link ResponseEntity}
     */
    @GetMapping()
    public ResponseEntity<?> serveAccessToken() {
        return accessService.serveAccessToken();
    }
}
