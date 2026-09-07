package com.strangequark.authservice.invitation;

import java.time.LocalDateTime;
import java.util.UUID;

public class InvitationResponse {
    private UUID id;
    private String token;
    private LocalDateTime expiresAt;

    public InvitationResponse(UUID id, String token, LocalDateTime expiresAt) {
        this.id = id;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public UUID getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
