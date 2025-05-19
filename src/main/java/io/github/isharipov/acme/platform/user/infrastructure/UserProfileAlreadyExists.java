package io.github.isharipov.acme.platform.user.infrastructure;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserProfileAlreadyExists extends RuntimeException {
    public UserProfileAlreadyExists(String message) {
        super(message);
    }
}
