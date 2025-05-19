package io.github.isharipov.acme.platform.auth.model;

import java.util.UUID;

public interface UserAuthProjection {

    UUID id();

    String email();

}
