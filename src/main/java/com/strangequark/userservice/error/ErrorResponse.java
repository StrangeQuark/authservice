package com.strangequark.userservice.error;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Response object for errors
 */
public class ErrorResponse {
    /**
     * Timestamp of when the error occurred
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private LocalDateTime timestamp;

    /**
     * Message included in the error
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String errorMessage;

    /**
     * Default constructor, set the timestamp
     */
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor if only errorMessage is passed
     */
    public ErrorResponse(String errorMessage) {
        this();
        this.errorMessage = errorMessage;
    }
}