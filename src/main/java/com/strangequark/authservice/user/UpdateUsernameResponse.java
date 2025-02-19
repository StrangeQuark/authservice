package com.strangequark.authservice.user;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Response object for updating username requests
 */
public class UpdateUsernameResponse {
    /**
     * Timestamp of the response
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private final LocalDateTime timestamp;

    /**
     * Refresh JWT sent back to the user
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String refreshToken;

    /**
     * Access JWT sent back to the user
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String accessToken;

    /**
     * Default constructor, set the timestamp
     */
    public UpdateUsernameResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor if a JWT token is being returned
     */
    public UpdateUsernameResponse(String refreshToken, String accessToken) {
        this();
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }
}
