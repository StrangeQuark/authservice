package com.strangequark.authservice.serviceaccount;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth/service-account")
public class ServiceAccountController {
    private final ServiceAccountService serviceAccountService;

    public ServiceAccountController(ServiceAccountService serviceAccountService) {
        this.serviceAccountService = serviceAccountService;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody ServiceAccountRequest serviceAccountRequest) {
        return serviceAccountService.authenticate(serviceAccountRequest);
    }
}
