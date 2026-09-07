package com.strangequark.authservice.invitation;

import java.util.UUID;

public class InvitationRequest {
    private String email;
    private UUID id;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
