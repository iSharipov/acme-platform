package io.github.isharipov.acme.platform.external.dto;

import java.util.UUID;

public record ExternalProjectUpdateInboundDto(String name, UUID userId) {
}
