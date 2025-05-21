package io.github.isharipov.acme.platform.user.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record UserProfileInboundDto(@NotBlank(message = "Name cannot be blank") String name) {
}
