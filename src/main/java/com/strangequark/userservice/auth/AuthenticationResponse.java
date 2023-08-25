package com.strangequark.userservice.auth;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response object for authentication requests
 */
public class AuthenticationResponse {
    /**
     * Timestamp of when the error occurred
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private LocalDateTime timestamp;

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
     * Constructor if only errorMessage is passed
     */
    public AuthenticationResponse(String jwtToken) {
        this();
        this.jwtToken = jwtToken;
    }
}
