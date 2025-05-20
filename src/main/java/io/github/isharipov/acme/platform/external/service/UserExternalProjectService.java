package io.github.isharipov.acme.platform.external.service;

import io.github.isharipov.acme.platform.user.dto.UserProfileOutboundDto;
import io.github.isharipov.acme.platform.external.dto.ExternalProjectInboundDto;
import io.github.isharipov.acme.platform.external.dto.ExternalProjectOutboundDto;
import io.github.isharipov.acme.platform.external.dto.ExternalProjectUpdateInboundDto;
import io.github.isharipov.acme.platform.external.infrastructure.mapper.UserExternalProjectMapper;
import io.github.isharipov.acme.platform.external.model.UserExternalProject;
import io.github.isharipov.acme.platform.external.repository.UserExternalProjectRepository;
import io.github.isharipov.acme.platform.user.service.UserProfileService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserExternalProjectService {

    private final UserExternalProjectRepository userExternalProjectRepository;
    private final UserExternalProjectMapper userExternalProjectMapper;
    private final UserProfileService userProfileService;

    public UserExternalProjectService(UserExternalProjectRepository userExternalProjectRepository, UserExternalProjectMapper userExternalProjectMapper, UserProfileService userProfileService) {
        this.userExternalProjectRepository = userExternalProjectRepository;
        this.userExternalProjectMapper = userExternalProjectMapper;
        this.userProfileService = userProfileService;
    }

    public ExternalProjectOutboundDto createExternalProject(ExternalProjectInboundDto externalProject) {
        UserExternalProject userExternalProject = userExternalProjectMapper.toUserExternalProject(externalProject);
        if (externalProject.userId() != null) {
            UserProfileOutboundDto userProfile = userProfileService.getUserById(externalProject.userId());
            userExternalProject.setUserId(userProfile.id());
        }
        return userExternalProjectMapper.toExternalProjectOutbound(userExternalProjectRepository.save(userExternalProject));
    }

    public ExternalProjectOutboundDto updateExternalProject(UUID externalProjectId, ExternalProjectUpdateInboundDto externalProjectRequest) {
        UserExternalProject externalProject = userExternalProjectRepository.findById(externalProjectId)
                .orElseThrow(() -> new EntityNotFoundException("External Project not found"));
        if (externalProjectRequest.userId() != null) {
            userProfileService.getUserById(externalProjectRequest.userId());
        }
        userExternalProjectMapper.updateFromDto(externalProjectRequest, externalProject);
        return userExternalProjectMapper.toExternalProjectOutbound(userExternalProjectRepository.save(externalProject));
    }

    public Page<ExternalProjectOutboundDto> getUserProjects(UUID userId, Pageable pageable) {
        return userExternalProjectRepository.findAllByUserId(userId, pageable)
                .map(userExternalProjectMapper::toExternalProjectOutbound);
    }
}
