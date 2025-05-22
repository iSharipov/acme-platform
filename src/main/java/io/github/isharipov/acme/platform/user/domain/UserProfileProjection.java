package io.github.isharipov.acme.platform.user.domain;

import java.util.UUID;

public interface UserProfileProjection {
    UUID getId();

    UUID getAuthId();

    String getName();
}
