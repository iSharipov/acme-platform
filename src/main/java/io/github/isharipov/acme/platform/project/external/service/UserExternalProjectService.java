package io.github.isharipov.acme.platform.project.external.service;

import io.github.isharipov.acme.platform.project.external.rest.dto.ExternalProjectInboundDto;
import io.github.isharipov.acme.platform.project.external.rest.dto.ExternalProjectOutboundDto;
import io.github.isharipov.acme.platform.project.external.rest.dto.ExternalProjectUpdateInboundDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserExternalProjectService {
    ExternalProjectOutboundDto createExternalProject(ExternalProjectInboundDto externalProject);

    ExternalProjectOutboundDto updateExternalProject(UUID externalProjectId, ExternalProjectUpdateInboundDto externalProjectRequest);

    Page<ExternalProjectOutboundDto> getUserProjects(UUID userId, Pageable pageable);
}
