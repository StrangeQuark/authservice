package com.strangequark.authservice.error;

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
     * Message included in the error
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private int errorCode;

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

    /**
     * Constructor if only errorCode is passed
     */
    public ErrorResponse(int errorCode) {
        this();
        this.errorCode = errorCode;
    }

    /**
     * Constructor if both errorMessage and errorCode are passed
     */
    public ErrorResponse(String errorMessage, int errorCode) {
        this(errorMessage);
        this.errorCode = errorCode;
    }
}