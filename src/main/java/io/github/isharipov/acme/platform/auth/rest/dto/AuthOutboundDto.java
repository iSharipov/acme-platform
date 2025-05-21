package io.github.isharipov.acme.platform.auth.rest.dto;

import io.github.isharipov.acme.platform.common.dto.TokenOutboundDto;

public record AuthOutboundDto(UserAuthOutboundDto user, TokenOutboundDto token) {

}
