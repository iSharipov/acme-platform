package io.github.isharipov.acme.platform.user.infrastructure.mapper;

import io.github.isharipov.acme.platform.user.rest.dto.CreateUserProfileInboundDto;
import io.github.isharipov.acme.platform.user.rest.dto.UserProfileInboundDto;
import io.github.isharipov.acme.platform.user.rest.dto.UserProfileOutboundDto;
import io.github.isharipov.acme.platform.user.domain.UserProfile;
import io.github.isharipov.acme.platform.user.domain.UserProfileProjection;
import org.mapstruct.*;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserProfileMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "name", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "deleted", ignore = true)
    })
    UserProfile createUserToUser(CreateUserProfileInboundDto createUser);

    @Mappings(@Mapping(target = "email", ignore = true))
    UserProfileOutboundDto toOutboundUserDto(UserProfile user);

    UserProfileOutboundDto toOutboundUserDto(UserProfile user, String email);

    @Mappings(@Mapping(target = "email", ignore = true))
    UserProfileOutboundDto toOutboundUserDto(UserProfileProjection user);

    UserProfileOutboundDto toOutboundUserDto(UserProfileProjection user, String email);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "authId", ignore = true),
            @Mapping(target = "deleted", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true)
    })
    void updateFromDto(UserProfileInboundDto request, @MappingTarget UserProfile userProfile);

}
