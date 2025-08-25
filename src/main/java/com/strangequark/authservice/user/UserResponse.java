package com.strangequark.authservice.user;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.UUID;

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
     * ID of the user
     */
    private UUID userId;

    /**
     * Username of the user
     */
    private String username;

    /**
     * Email of the user
     */
    private String email;

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

    /**
     * Get user ID
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Set user ID
     */
    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    /**
     * Get username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set email
     */
    public void setEmail(String email) {
        this.email = email;
    }
}
