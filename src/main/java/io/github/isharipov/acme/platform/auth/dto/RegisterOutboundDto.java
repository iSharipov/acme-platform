package io.github.isharipov.acme.platform.auth.dto;

import java.util.UUID;

public record RegisterOutboundDto(UUID id, String email) {
}
