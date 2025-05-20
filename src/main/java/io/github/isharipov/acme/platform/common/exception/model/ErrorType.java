package io.github.isharipov.acme.platform.common.exception.model;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public enum ErrorType {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation error"),
    JSON_PATH_NOT_FOUND(HttpStatus.BAD_REQUEST, "Json path not found"),
    ENTITY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Entity error"),
    HTTP_MESSAGE_NOT_READABLE(HttpStatus.UNPROCESSABLE_ENTITY, "Http Message Not Readable"),
    USER_ALREADY_REGISTERED_ERROR(HttpStatus.CONFLICT, "User is already registered"),
    AUTH_ERROR(HttpStatus.UNAUTHORIZED, "Authentication failed"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access denied");

    private final HttpStatus httpStatus;
    private final String message;


    ErrorType(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public ResponseEntity<ErrorResponse> getErrorResponse() {
        return ResponseEntity.status(httpStatus).body(new ErrorResponse(message));
    }

    public ResponseEntity<ErrorResponse> getErrorResponse(String description) {
        return ResponseEntity.status(httpStatus).body(new ErrorResponse(message, description));
    }


    public ResponseEntity<ErrorResponse> getErrorResponse(List<ApiError> errors) {
        return ResponseEntity.status(httpStatus).body(new ErrorResponse(message, errors));
    }

    public ResponseEntity<ErrorResponse> getErrorResponse(String description, List<ApiError> errors) {
        return ResponseEntity.status(httpStatus).body(new ErrorResponse(message, description, errors));
    }

    public ResponseEntity<ErrorResponse> getErrorResponse(HttpStatus httpStatus, String message) {
        return ResponseEntity.status(httpStatus).body(new ErrorResponse(message));
    }

    public ResponseEntity<ErrorResponse> getErrorResponse(HttpStatus httpStatus, String message, String description) {
        return ResponseEntity.status(httpStatus).body(new ErrorResponse(message, description));
    }

    public ResponseEntity<ErrorResponse> getErrorResponse(HttpStatus httpStatus, String message, List<ApiError> errors) {
        return ResponseEntity.status(httpStatus).body(new ErrorResponse(message, errors));
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}
