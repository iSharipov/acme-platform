package io.github.isharipov.acme.platform.auth.service;

import io.github.isharipov.acme.platform.auth.dto.AuthInboundDto;
import io.github.isharipov.acme.platform.auth.dto.AuthOutboundDto;
import io.github.isharipov.acme.platform.auth.dto.RegisterInboundDto;

public interface AuthService {

    AuthOutboundDto register(RegisterInboundDto registerRequest);

    AuthOutboundDto login(AuthInboundDto authRequest);
}
