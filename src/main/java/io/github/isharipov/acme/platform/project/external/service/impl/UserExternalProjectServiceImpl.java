package io.github.isharipov.acme.platform.project.external.service.impl;

import io.github.isharipov.acme.platform.project.external.dto.ExternalProjectInboundDto;
import io.github.isharipov.acme.platform.project.external.dto.ExternalProjectOutboundDto;
import io.github.isharipov.acme.platform.project.external.dto.ExternalProjectUpdateInboundDto;
import io.github.isharipov.acme.platform.project.external.infrastructure.mapper.UserExternalProjectMapper;
import io.github.isharipov.acme.platform.project.external.repository.UserExternalProjectRepository;
import io.github.isharipov.acme.platform.project.external.service.UserExternalProjectService;
import io.github.isharipov.acme.platform.user.service.UserProfileService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserExternalProjectServiceImpl implements UserExternalProjectService {

    private static final Logger logger = LoggerFactory.getLogger(UserExternalProjectServiceImpl.class);

    private final UserExternalProjectRepository userExternalProjectRepository;
    private final UserExternalProjectMapper userExternalProjectMapper;
    private final UserProfileService userProfileService;

    public UserExternalProjectServiceImpl(UserExternalProjectRepository userExternalProjectRepository, UserExternalProjectMapper userExternalProjectMapper, UserProfileService userProfileService) {
        this.userExternalProjectRepository = userExternalProjectRepository;
        this.userExternalProjectMapper = userExternalProjectMapper;
        this.userProfileService = userProfileService;
    }

    public ExternalProjectOutboundDto createExternalProject(ExternalProjectInboundDto externalProject) {
        logger.info("Creating external project with externalId={}, userId={}",
                externalProject.externalId(), externalProject.userId());
        var userExternalProject = userExternalProjectMapper.toUserExternalProject(externalProject);
        if (externalProject.userId() != null) {
            var userProfile = userProfileService.getUserById(externalProject.userId());
            userExternalProject.setUserId(userProfile.id());
            logger.debug("Associated user profile found for userId={}", userProfile.id());
        }

        var savedProject = userExternalProjectRepository.save(userExternalProject);

        logger.info("External project created: id={}, externalId={}, userId={}",
                savedProject.getId(), savedProject.getExternalId(), savedProject.getUserId());

        return userExternalProjectMapper.toExternalProjectOutbound(savedProject);
    }

    public ExternalProjectOutboundDto updateExternalProject(UUID externalProjectId, ExternalProjectUpdateInboundDto externalProjectRequest) {
        logger.info("Updating external project with id={}", externalProjectId);
        var externalProject = userExternalProjectRepository.findById(externalProjectId)
                .orElseThrow(() -> {
                    logger.warn("External project not found for update: id={}", externalProjectId);
                    return new EntityNotFoundException("External Project not found");
                });
        if (externalProjectRequest.userId() != null) {
            userProfileService.getUserById(externalProjectRequest.userId());
            logger.debug("Validated user exists for userId={}", externalProjectRequest.userId());
        }
        userExternalProjectMapper.updateFromDto(externalProjectRequest, externalProject);
        var updated = userExternalProjectRepository.save(externalProject);

        logger.info("External project updated: id={}, externalId={}, userId={}", updated.getId(), updated.getExternalId(), updated.getUserId());
        return userExternalProjectMapper.toExternalProjectOutbound(updated);
    }

    public Page<ExternalProjectOutboundDto> getUserProjects(UUID userId, Pageable pageable) {
        logger.info("Fetching external projects for userId={}, page={}", userId, pageable.getPageNumber());
        return userExternalProjectRepository.findAllByUserId(userId, pageable)
                .map(userExternalProjectMapper::toExternalProjectOutbound);
    }
}
