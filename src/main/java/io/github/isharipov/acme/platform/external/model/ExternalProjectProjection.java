package io.github.isharipov.acme.platform.external.model;

import java.util.UUID;

public interface ExternalProjectProjection {
    UUID getId();

    String getExternalId();

    String getName();
}