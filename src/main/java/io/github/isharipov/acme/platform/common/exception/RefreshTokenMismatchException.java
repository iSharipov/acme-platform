package io.github.isharipov.acme.platform.common.exception;

public class RefreshTokenMismatchException extends RuntimeException {
    public RefreshTokenMismatchException(String message) {
        super(message);
    }
}