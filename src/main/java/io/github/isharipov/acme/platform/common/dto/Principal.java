package io.github.isharipov.acme.platform.common.dto;

import java.util.UUID;

public record Principal(UUID authId, String email) {
}
