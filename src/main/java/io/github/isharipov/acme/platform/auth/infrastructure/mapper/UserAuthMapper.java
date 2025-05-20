package io.github.isharipov.acme.platform.auth.infrastructure.mapper;

import io.github.isharipov.acme.platform.auth.dto.RegisterInboundDto;
import io.github.isharipov.acme.platform.auth.dto.RegisterOutboundDto;
import io.github.isharipov.acme.platform.auth.dto.UserAuthOutboundDto;
import io.github.isharipov.acme.platform.auth.model.UserAuth;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper
public interface UserAuthMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "refreshToken", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
    })
    UserAuth toUserAuth(RegisterInboundDto registerRequest);

    UserAuthOutboundDto toUserAuthOutbound(UserAuth userAuth);

    RegisterOutboundDto toRegisterOutbound(UserAuth userAuth);
}
