package io.github.isharipov.acme.platform.auth.service;

import io.github.isharipov.acme.platform.auth.dto.AuthInboundDto;
import io.github.isharipov.acme.platform.auth.dto.AuthOutboundDto;
import io.github.isharipov.acme.platform.auth.dto.RegisterInboundDto;
import io.github.isharipov.acme.platform.auth.dto.TokenOutboundDto;

import java.util.UUID;

public interface AuthService {

    AuthOutboundDto register(RegisterInboundDto registerRequest);

    AuthOutboundDto login(AuthInboundDto authRequest);

    void deleteSelf(UUID authId);

    TokenOutboundDto refreshToken(String refreshToken);
}
