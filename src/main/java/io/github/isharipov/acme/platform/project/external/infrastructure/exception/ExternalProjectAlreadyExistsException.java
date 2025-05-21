package io.github.isharipov.acme.platform.project.external.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ExternalProjectAlreadyExistsException extends RuntimeException {
    public ExternalProjectAlreadyExistsException(String message) {
        super(message);
    }
}