package io.github.isharipov.acme.platform.auth.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthInboundDto(@NotBlank String login, @NotBlank String password) {
}
