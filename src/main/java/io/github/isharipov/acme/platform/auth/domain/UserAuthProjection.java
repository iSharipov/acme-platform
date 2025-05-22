package io.github.isharipov.acme.platform.auth.domain;

import java.util.UUID;

public interface UserAuthProjection {

    UUID id();

    String email();

}
