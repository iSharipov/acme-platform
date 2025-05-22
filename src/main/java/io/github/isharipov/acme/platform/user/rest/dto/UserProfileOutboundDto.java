package io.github.isharipov.acme.platform.user.rest.dto;

import java.util.UUID;

public record UserProfileOutboundDto(UUID id, String email, String name) {

}
