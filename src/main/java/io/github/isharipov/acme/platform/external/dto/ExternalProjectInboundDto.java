package io.github.isharipov.acme.platform.external.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ExternalProjectInboundDto(@NotBlank @Size(min = 1) String externalId,
                                        String name,
                                        UUID userId) {
}
