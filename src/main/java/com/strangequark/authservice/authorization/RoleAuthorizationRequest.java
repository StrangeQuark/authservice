package com.strangequark.authservice.authorization;

import com.strangequark.authservice.user.Role;

public class RoleAuthorizationRequest {
    private Role role;
    private String authorization;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }
}
