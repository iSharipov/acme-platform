package io.github.isharipov.acme.platform.project.external.dto;

import java.util.UUID;

public record ExternalProjectOutboundDto(UUID id, UUID userId, String externalId, String name) {
}
