package io.github.isharipov.acme.platform.project.external.rest.dto;

import java.util.UUID;

public record ExternalProjectUpdateInboundDto(String name, UUID userId) {
}
