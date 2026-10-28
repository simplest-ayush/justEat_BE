package com.justeat.backend.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class ErrorResponse {

    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final List<String> validationErrors;

    public ErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.validationErrors = null;
    }

    public ErrorResponse(int status, String error, String message, String path, List<String> validationErrors) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.validationErrors = validationErrors;
    }
}