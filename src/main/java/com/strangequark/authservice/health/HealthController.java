package com.strangequark.authservice.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@link RestController} responsible for health check
 */
@RestController
@RequestMapping("/auth/health")
public class HealthController {
    /**
     * Get request endpoint for healthcheck
     * @return {@link ResponseEntity}
     */
    @GetMapping()
    public ResponseEntity<String> healthcheck() {
        return ResponseEntity.ok("200 OK");
    }
}
