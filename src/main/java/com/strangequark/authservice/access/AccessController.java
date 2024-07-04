package com.strangequark.authservice.access;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * {@link RestController} for demoing the application
 */
@RestController
@RequestMapping("/api/v1/access")
@RequiredArgsConstructor
public class AccessController {
    /**
     * {@link AccessService}
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
