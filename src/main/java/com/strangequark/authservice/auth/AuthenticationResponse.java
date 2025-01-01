package com.strangequark.authservice.auth;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Response object for authentication requests
 */
public class AuthenticationResponse {
    /**
     * Timestamp of the response
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private final LocalDateTime timestamp;

    /**
     * JWT token sent back to the user
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String jwtToken;

    /**
     * Default constructor, set the timestamp
     */
    public AuthenticationResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor if a JWT token is being returned
     */
    public AuthenticationResponse(String jwtToken) {
        this();
        this.jwtToken = jwtToken;
    }
}
