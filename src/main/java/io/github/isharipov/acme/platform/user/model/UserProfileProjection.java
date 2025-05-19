package io.github.isharipov.acme.platform.user.model;

import java.util.UUID;

public interface UserProfileProjection {
    UUID getId();

    UUID getAuthId();

    String getName();
}
