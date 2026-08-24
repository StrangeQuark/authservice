package com.strangequark.authservice.authorization;

import com.strangequark.authservice.user.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/role-authorization")
public class RoleAuthorizationController {
    private final RoleAuthorizationService roleAuthorizationService;

    public RoleAuthorizationController(RoleAuthorizationService roleAuthorizationService) {
        this.roleAuthorizationService = roleAuthorizationService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addRoleAuthorization(@RequestBody RoleAuthorizationRequest request) {
        return roleAuthorizationService.addRoleAuthorization(request);
    }

    @GetMapping("/get")
    public ResponseEntity<?> getRoleAuthorizations(@RequestParam Role role) {
        return roleAuthorizationService.getRoleAuthorizations(role);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> removeRoleAuthorization(@RequestBody RoleAuthorizationRequest request) {
        return roleAuthorizationService.removeRoleAuthorization(request);
    }
}
