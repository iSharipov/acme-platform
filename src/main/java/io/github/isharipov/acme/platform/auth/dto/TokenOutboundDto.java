package io.github.isharipov.acme.platform.auth.dto;

public record TokenOutboundDto(String accessToken, String refreshToken) {
}
