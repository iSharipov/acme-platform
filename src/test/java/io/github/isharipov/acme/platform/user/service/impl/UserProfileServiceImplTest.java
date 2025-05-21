package io.github.isharipov.acme.platform.user.service.impl;

import com.github.javafaker.Faker;
import io.github.isharipov.acme.platform.common.dto.Principal;
import io.github.isharipov.acme.platform.project.external.service.UserExternalProjectService;
import io.github.isharipov.acme.platform.user.dto.UserProfileOutboundDto;
import io.github.isharipov.acme.platform.user.infrastructure.UserProfileAlreadyExists;
import io.github.isharipov.acme.platform.user.infrastructure.UserProfileNotFoundException;
import io.github.isharipov.acme.platform.user.infrastructure.mapper.UserProfileMapper;
import io.github.isharipov.acme.platform.user.model.UserProfile;
import io.github.isharipov.acme.platform.user.model.UserProfileProjection;
import io.github.isharipov.acme.platform.user.repository.UserProfileRepository;
import io.github.isharipov.acme.platform.user.rest.dto.CreateUserProfileInboundDto;
import io.github.isharipov.acme.platform.user.rest.dto.UserProfileInboundDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserProfileMapper userProfileMapper;

    @Mock
    private UserExternalProjectService userExternalProjectService;

    @InjectMocks
    private UserProfileServiceImpl service;

    private final Principal principal = new Principal(UUID.randomUUID(), new Faker().internet().emailAddress());
    private final UUID id = UUID.randomUUID();

    @Test
    void createUserProfile_shouldThrow_whenProfileExists() {
        // GIVEN
        var inbound = new CreateUserProfileInboundDto(principal.authId());
        var existingProfile = new UserProfile();
        existingProfile.setAuthId(principal.authId());
        existingProfile.setDeleted(false);
        // WHEN
        when(userProfileRepository.findEntityByAuthId(principal.authId())).thenReturn(Optional.of(existingProfile));
        // THEN
        assertThrows(UserProfileAlreadyExists.class, () -> service.createUserProfile(inbound));
    }

    @Test
    void getUserProfileByAuthId_shouldThrow_whenNotFound() {
        // GIVEN
        // WHEN
        when(userProfileRepository.findByAuthIdAndDeletedFalse(principal.authId())).thenReturn(Optional.empty());
        // THEN
        assertThrows(UserProfileNotFoundException.class, () -> service.getUserProfileByAuthId(principal));
    }

    @Test
    void getUserById_shouldThrow_whenNotFound() {
        // GIVEN
        // WHEN
        when(userProfileRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());
        // THEN
        assertThrows(UserProfileNotFoundException.class, () -> service.getUserById(id));
    }

    @Test
    void softDeleteUserProfile_shouldThrow_whenNotFound() {
        // GIVEN
        // WHEN
        when(userProfileRepository.findEntityByAuthIdAndDeletedFalse(principal.authId())).thenReturn(Optional.empty());
        // THEN
        assertThrows(UserProfileNotFoundException.class, () -> service.softDeleteUserProfile(principal.authId()));
    }

    @Test
    void createOrRestoreUserProfile_shouldCreate_whenProfileNotFound() {
        // GIVEN
        var saved = new UserProfile();
        saved.setAuthId(principal.authId());
        saved.setDeleted(false);
        // WHEN
        when(userProfileRepository.save(any())).thenReturn(saved);
        when(userProfileRepository.findEntityByAuthId(principal.authId())).thenReturn(Optional.empty());
        when(userProfileMapper.toOutboundUserDto(any(UserProfile.class))).thenReturn(mock(UserProfileOutboundDto.class));
        // THEN
        service.createOrRestoreUserProfile(principal.authId());

        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    void createOrRestoreUserProfile_shouldRestore_whenProfileIsDeleted() {
        // GIVEN
        var existing = new UserProfile();
        existing.setAuthId(principal.authId());
        existing.setDeleted(true);
        // WHEN
        when(userProfileRepository.findEntityByAuthId(principal.authId())).thenReturn(Optional.of(existing));
        when(userProfileRepository.save(any())).thenReturn(existing);
        when(userProfileMapper.toOutboundUserDto(any(UserProfile.class))).thenReturn(mock(UserProfileOutboundDto.class));
        // THEN
        service.createOrRestoreUserProfile(principal.authId());

        assertFalse(existing.isDeleted());
    }

    @Test
    void createUserProfile_shouldCreate_whenNotExists() {
        // GIVEN
        var inbound = new CreateUserProfileInboundDto(principal.authId());
        var newProfile = new UserProfile();
        newProfile.setAuthId(principal.authId());
        newProfile.setDeleted(false);
        // WHEN
        when(userProfileRepository.findEntityByAuthId(principal.authId())).thenReturn(Optional.empty());
        when(userProfileMapper.createUserToUser(inbound)).thenReturn(newProfile);
        when(userProfileRepository.save(newProfile)).thenReturn(newProfile);
        when(userProfileMapper.toOutboundUserDto(newProfile)).thenReturn(mock(UserProfileOutboundDto.class));
        // THEN
        service.createUserProfile(inbound);

        verify(userProfileRepository).save(newProfile);
    }

    @Test
    void createUserProfile_shouldRestore_whenProfileIsDeleted() {
        // GIVEN
        var inbound = new CreateUserProfileInboundDto(principal.authId());
        var existing = new UserProfile();
        existing.setAuthId(principal.authId());
        existing.setDeleted(true);
        // WHEN
        when(userProfileRepository.findEntityByAuthId(principal.authId())).thenReturn(Optional.of(existing));
        when(userProfileRepository.save(existing)).thenReturn(existing);
        when(userProfileMapper.toOutboundUserDto(existing)).thenReturn(mock(UserProfileOutboundDto.class));
        // THEN
        service.createUserProfile(inbound);

        assertFalse(existing.isDeleted());
        verify(userProfileRepository).save(existing);
    }

    @Test
    void softDeleteUserProfile_shouldMarkDeleted_whenExists() {
        // GIVEN
        var existing = new UserProfile();
        existing.setAuthId(principal.authId());
        existing.setDeleted(false);
        // WHEN
        when(userProfileRepository.findEntityByAuthIdAndDeletedFalse(principal.authId())).thenReturn(Optional.of(existing));
        when(userProfileRepository.save(existing)).thenReturn(existing);
        // THEN
        service.softDeleteUserProfile(principal.authId());

        assertTrue(existing.isDeleted());
        verify(userProfileRepository).save(existing);
    }

    @Test
    void updateUserProfile_shouldUpdate_whenExists() {
        // GIVEN
        var inbound = mock(UserProfileInboundDto.class);
        var profile = new UserProfile();
        profile.setAuthId(principal.authId());
        // WHEN
        when(userProfileRepository.findEntityByAuthIdAndDeletedFalse(principal.authId())).thenReturn(Optional.of(profile));
        when(userProfileRepository.save(profile)).thenReturn(profile);
        when(userProfileMapper.toOutboundUserDto(profile, principal.email())).thenReturn(mock(UserProfileOutboundDto.class));
        // THEN
        service.updateUserProfile(principal, inbound);

        verify(userProfileMapper).updateFromDto(inbound, profile);
        verify(userProfileRepository).save(profile);
    }

    @Test
    void getUserProfileByAuthId_shouldReturn_whenExists() {
        // GIVEN
        var projection = mock(UserProfileProjection.class);
        // WHEN
        when(userProfileRepository.findByAuthIdAndDeletedFalse(principal.authId())).thenReturn(Optional.of(projection));
        when(userProfileMapper.toOutboundUserDto(projection, principal.email())).thenReturn(mock(UserProfileOutboundDto.class));
        // THEN
        service.getUserProfileByAuthId(principal);

        verify(userProfileMapper).toOutboundUserDto(projection, principal.email());
    }
}
