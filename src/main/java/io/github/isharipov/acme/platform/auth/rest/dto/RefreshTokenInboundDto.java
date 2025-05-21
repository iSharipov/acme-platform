package io.github.isharipov.acme.platform.auth.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenInboundDto(@NotBlank String refreshToken) {
}