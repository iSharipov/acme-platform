package io.github.isharipov.acme.platform.common.exception.model;

import java.util.Collections;
import java.util.List;

public class ErrorResponse extends StandardResponse {
    private final String message;
    private final String description;
    private final List<ApiError> errors;

    public ErrorResponse(String message) {
        super(false);
        this.message = message;
        this.description = "";
        this.errors = Collections.emptyList();
    }

    public ErrorResponse(String message, List<ApiError> errors) {
        super(false);
        this.message = message;
        this.description = "";
        this.errors = errors;
    }

    public ErrorResponse(String message, String description) {
        super(false);
        this.message = message;
        this.description = description;
        this.errors = Collections.emptyList();
    }

    public ErrorResponse(String message, String description, List<ApiError> errors) {
        super(false);
        this.message = message;
        this.description = description;
        this.errors = errors;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }

    public List<ApiError> getErrors() {
        return errors;
    }
}
