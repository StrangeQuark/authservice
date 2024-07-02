package com.strangequark.authservice.user;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class UserResponse {
    /**
     * Timestamp of when the error occurred
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private LocalDateTime timestamp;

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
     * Constructor if only message is passed
     */
    public UserResponse(String message) {
        this();
        this.message = message;
    }
}
