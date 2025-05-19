package io.github.isharipov.acme.platform.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthInboundDto(@NotBlank String login, @NotBlank String password) {
}
