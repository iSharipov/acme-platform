package io.github.isharipov.acme.platform.user.infrastructure.mapper;

import io.github.isharipov.acme.platform.user.dto.CreateUserProfileInboundDto;
import io.github.isharipov.acme.platform.user.dto.UserProfileOutboundDto;
import io.github.isharipov.acme.platform.user.model.UserProfile;
import io.github.isharipov.acme.platform.user.model.UserProfileProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper
public interface UserMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "name", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "deleted", ignore = true)
    })
    UserProfile createUserToUser(CreateUserProfileInboundDto createUser);

    UserProfileOutboundDto toOutboundUserDto(UserProfile user);

    UserProfileOutboundDto toOutboundUserDto(UserProfileProjection user);

}
