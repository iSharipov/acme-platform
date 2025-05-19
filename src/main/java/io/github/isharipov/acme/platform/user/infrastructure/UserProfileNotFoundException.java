package io.github.isharipov.acme.platform.user.infrastructure;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserProfileNotFoundException extends RuntimeException {
    public UserProfileNotFoundException(String message) {
        super(message);
    }
}
