package com.strangequark.authservice.authorization;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/authorization")
public class AuthorizationController {
    private final AuthorizationService authorizationService;

    public AuthorizationController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createAuthorization(@RequestBody AuthorizationRequest request) {
        return authorizationService.createAuthorization(request);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAuthorizations() {
        return authorizationService.getAuthorizations();
    }

    @DeleteMapping("/delete/{name}")
    public ResponseEntity<?> deleteAuthorization(@PathVariable String name) {
        return authorizationService.deleteAuthorization(name);
    }
}
