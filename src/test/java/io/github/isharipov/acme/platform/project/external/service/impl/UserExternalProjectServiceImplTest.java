package io.github.isharipov.acme.platform.project.external.service.impl;

import io.github.isharipov.acme.platform.project.external.rest.dto.ExternalProjectOutboundDto;
import io.github.isharipov.acme.platform.project.external.infrastructure.mapper.UserExternalProjectMapper;
import io.github.isharipov.acme.platform.project.external.model.UserExternalProject;
import io.github.isharipov.acme.platform.project.external.repository.UserExternalProjectRepository;
import io.github.isharipov.acme.platform.project.external.rest.dto.ExternalProjectInboundDto;
import io.github.isharipov.acme.platform.project.external.rest.dto.ExternalProjectUpdateInboundDto;
import io.github.isharipov.acme.platform.user.rest.dto.UserProfileOutboundDto;
import io.github.isharipov.acme.platform.user.service.UserProfileService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserExternalProjectServiceImplTest {

    @Mock
    private UserExternalProjectRepository userExternalProjectRepository;
    @Mock
    private UserExternalProjectMapper userExternalProjectMapper;
    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private UserExternalProjectServiceImpl service;

    private final UUID projectId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    void createExternalProject_shouldCallUserService_whenUserIdIsPresent() {
        // GIVEN
        var inbound = new ExternalProjectInboundDto("ext-123", "name", userId);
        var mapped = new UserExternalProject();
        var saved = new UserExternalProject();
        saved.setId(UUID.randomUUID());
        saved.setExternalId("ext-123");
        saved.setUserId(userId);
        // WHEN
        when(userExternalProjectMapper.toUserExternalProject(inbound)).thenReturn(mapped);
        when(userProfileService.getUserById(userId)).thenReturn(new UserProfileOutboundDto(userId, "email", "name"));
        when(userExternalProjectRepository.save(ArgumentMatchers.any())).thenReturn(saved);
        when(userExternalProjectMapper.toExternalProjectOutbound(any(UserExternalProject.class))).thenReturn(mock(ExternalProjectOutboundDto.class));
        // THEN
        service.createExternalProject(inbound);

        verify(userProfileService).getUserById(userId);
        verify(userExternalProjectRepository).save(mapped);
    }

    @Test
    void updateExternalProject_shouldThrow_whenProjectNotFound() {
        // GIVEN
        var inbound = new ExternalProjectUpdateInboundDto("ext-456", userId);
        // WHEN
        when(userExternalProjectRepository.findById(projectId)).thenReturn(Optional.empty());
        // THEN
        assertThrows(EntityNotFoundException.class, () -> service.updateExternalProject(projectId, inbound));
    }

    @Test
    void updateExternalProject_shouldValidateUser_whenUserIdPresent() {
        // GIVEN
        var inbound = new ExternalProjectUpdateInboundDto("ext-456", userId);
        var project = new UserExternalProject();
        // WHEN
        when(userExternalProjectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userExternalProjectRepository.save(any())).thenReturn(project);
        when(userExternalProjectMapper.toExternalProjectOutbound(any(UserExternalProject.class)))
                .thenReturn(mock(ExternalProjectOutboundDto.class));
        // THEN
        service.updateExternalProject(projectId, inbound);

        verify(userProfileService).getUserById(userId);
        verify(userExternalProjectMapper).updateFromDto(inbound, project);
        verify(userExternalProjectRepository).save(project);
    }
}
