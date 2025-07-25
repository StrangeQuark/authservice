package com.strangequark.authservice.user;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Response object for user requests
 */
public class UserResponse {
    /**
     * Timestamp of the response
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private final LocalDateTime timestamp;

    /**
     * Response message sent back to the user
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String message;

    /**
     * Default constructor, set the timestamp
     */
    public UserResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor if only a message is passed
     */
    public UserResponse(String message) {
        this();
        this.message = message;
    }

    /**
     * Get {@link LocalDateTime} timestamp for the response object
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Get message for the response object
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set message for the response object
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
