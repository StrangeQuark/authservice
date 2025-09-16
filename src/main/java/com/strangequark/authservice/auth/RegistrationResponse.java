package com.strangequark.authservice.auth;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Response object for registration requests
 */
public class RegistrationResponse {
    /**
     * Timestamp of the response
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private final LocalDateTime timestamp;

    /**
     * Message sent back to the user
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String message;

    /**
     * Default constructor, set the timestamp
     */
    public RegistrationResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor if a message is being returned
     */
    public RegistrationResponse(String message) {
        this();
        this.message = message;
    }

    /**
     * Getter for timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Getter for message
     */
    public String getMessage() {
        return message;
    }
}
