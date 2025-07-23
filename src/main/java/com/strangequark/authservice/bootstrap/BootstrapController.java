package com.strangequark.authservice.bootstrap;

import com.strangequark.authservice.auth.RegistrationRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/internal/bootstrap")
public class BootstrapController {

    private final BootstrapService bootstrapService;

    public BootstrapController(BootstrapService bootstrapService) {
        this.bootstrapService = bootstrapService;
    }

    @PostMapping()
    public ResponseEntity<?> bootstrap(@RequestHeader("X-BOOTSTRAP-SECRET") String headerSecret, @RequestBody RegistrationRequest registrationRequest) {
        return bootstrapService.bootstrap(headerSecret, registrationRequest);
    }
}
