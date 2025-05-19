package io.github.isharipov.acme.platform.auth.dto;

public record AuthOutboundDto(UserAuthOutboundDto user, TokenOutboundDto token) {

}
