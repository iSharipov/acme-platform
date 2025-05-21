package io.github.isharipov.acme.platform.project.external.infrastructure.mapper;

import io.github.isharipov.acme.platform.project.external.dto.ExternalProjectOutboundDto;
import io.github.isharipov.acme.platform.project.external.model.ExternalProjectProjection;
import io.github.isharipov.acme.platform.project.external.model.UserExternalProject;
import io.github.isharipov.acme.platform.project.external.rest.dto.ExternalProjectInboundDto;
import io.github.isharipov.acme.platform.project.external.rest.dto.ExternalProjectUpdateInboundDto;
import org.mapstruct.*;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserExternalProjectMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true)
    })
    UserExternalProject toUserExternalProject(ExternalProjectInboundDto externalProject);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "externalId", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true)
    })
    UserExternalProject toUserExternalProject(ExternalProjectUpdateInboundDto externalProject);

    ExternalProjectOutboundDto toExternalProjectOutbound(UserExternalProject userExternalProject);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "externalId", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true)
    })
    void updateFromDto(ExternalProjectUpdateInboundDto request, @MappingTarget UserExternalProject userExternalProject);

    ExternalProjectOutboundDto toExternalProjectOutbound(ExternalProjectProjection externalProjectProjection);

}
