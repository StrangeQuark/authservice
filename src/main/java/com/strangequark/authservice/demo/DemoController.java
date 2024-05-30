package com.strangequark.authservice.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * {@link RestController} for demoing the application
 */
@RestController
@RequestMapping("/api/v1/demo")
@RequiredArgsConstructor
public class DemoController {

    /**
     * Get request endpoint for demoing authorization
     * @return {@link ResponseEntity}
     */
    @GetMapping()
    public ResponseEntity<String> demo() {
        return ResponseEntity.ok("Authorization demo");
    }
}
