package io.github.isharipov.acme.platform.project.external.rest.dto;

import java.util.UUID;

public record ExternalProjectOutboundDto(UUID id, UUID userId, String externalId, String name) {
}
