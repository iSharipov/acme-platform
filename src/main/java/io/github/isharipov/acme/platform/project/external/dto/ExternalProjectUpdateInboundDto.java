package io.github.isharipov.acme.platform.project.external.dto;

import java.util.UUID;

public record ExternalProjectUpdateInboundDto(String name, UUID userId) {
}
