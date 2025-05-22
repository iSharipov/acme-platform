package io.github.isharipov.acme.platform.user.service.impl;

import io.github.isharipov.acme.platform.common.dto.Principal;
import io.github.isharipov.acme.platform.user.rest.dto.UserProfileOutboundDto;
import io.github.isharipov.acme.platform.user.infrastructure.UserProfileAlreadyExists;
import io.github.isharipov.acme.platform.user.infrastructure.UserProfileNotFoundException;
import io.github.isharipov.acme.platform.user.infrastructure.mapper.UserProfileMapper;
import io.github.isharipov.acme.platform.user.domain.UserProfile;
import io.github.isharipov.acme.platform.user.repository.UserProfileRepository;
import io.github.isharipov.acme.platform.user.rest.dto.CreateUserProfileInboundDto;
import io.github.isharipov.acme.platform.user.rest.dto.UserProfileInboundDto;
import io.github.isharipov.acme.platform.user.service.UserProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileServiceImpl.class);

    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;


    public UserProfileServiceImpl(UserProfileRepository userProfileRepository, UserProfileMapper userProfileMapper) {
        this.userProfileRepository = userProfileRepository;
        this.userProfileMapper = userProfileMapper;
    }

    @Override
    public UserProfileOutboundDto createUserProfile(CreateUserProfileInboundDto createUserProfile) {
        var authId = createUserProfile.authId();
        logger.info("Creating or restoring user profile for authId={}", authId);

        var optionalProfile = userProfileRepository.findEntityByAuthId(authId);

        if (optionalProfile.isPresent()) {
            var profile = optionalProfile.get();

            if (!profile.isDeleted()) {
                logger.warn("User profile already exists for authId={}", authId);
                throw new UserProfileAlreadyExists(
                        "User Profile for the User with Id " + authId + " already exists");
            }

            logger.info("Restoring soft-deleted profile for authId={}", authId);
            profile.setDeleted(false);
            var restored = userProfileRepository.save(profile);
            return userProfileMapper.toOutboundUserDto(restored);
        }

        var newProfile = userProfileMapper.createUserToUser(createUserProfile);
        var saved = userProfileRepository.save(newProfile);
        logger.info("New user profile created: id={}, authId={}", saved.getId(), saved.getAuthId());
        return userProfileMapper.toOutboundUserDto(saved);
    }

    @Override
    public UserProfileOutboundDto getUserProfileByAuthId(Principal principal) {
        logger.info("Fetching user profile by authId={}", principal.authId());
        var userProfile = userProfileRepository.findByAuthIdAndDeletedFalse(principal.authId())
                .orElseThrow(() -> {
                    logger.warn("User profile not found for authId={}", principal.authId());
                    return new UserProfileNotFoundException("User Profile not found");
                });
        return userProfileMapper.toOutboundUserDto(userProfile, principal.email());
    }

    @Override
    public UserProfileOutboundDto getUserById(UUID id) {
        logger.info("Fetching user profile by id={}", id);
        var userProfile = userProfileRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> {
                    logger.warn("User profile not found for id={}", id);
                    return new UserProfileNotFoundException("User Profile not found");
                });
        return userProfileMapper.toOutboundUserDto(userProfile);
    }

    @Override
    public void softDeleteUserProfile(UUID authId) {
        logger.info("Soft-deleting user profile for authId={}", authId);
        var userProfile = userProfileRepository.findEntityByAuthIdAndDeletedFalse(authId)
                .orElseThrow(() -> {
                    logger.warn("User profile not found for soft-delete: authId={}", authId);
                    return new UserProfileNotFoundException("User Profile not found");
                });
        userProfile.setDeleted(true);
        userProfileRepository.save(userProfile);
        logger.info("User profile marked as deleted: id={}, authId={}", userProfile.getId(), userProfile.getAuthId());
    }

    @Override
    public UserProfileOutboundDto createOrRestoreUserProfile(UUID authId) {
        logger.info("Creating or restoring profile for authId={}", authId);

        var userProfile = userProfileRepository.findEntityByAuthId(authId)
                .orElseGet(() -> {
                    logger.info("No profile found, creating new for authId={}", authId);
                    var newProfile = new UserProfile();
                    newProfile.setAuthId(authId);
                    newProfile.setDeleted(false);
                    return newProfile;
                });

        if (userProfile.isDeleted()) {
            logger.info("Restoring soft-deleted profile for authId={}", authId);
            userProfile.setDeleted(false);
        }

        var saved = userProfileRepository.save(userProfile);
        return userProfileMapper.toOutboundUserDto(saved);
    }

    @Override
    public UserProfileOutboundDto updateUserProfile(Principal principal, UserProfileInboundDto request) {
        logger.info("Update user profile for authId={}", principal.authId());
        var userProfile = userProfileRepository.findEntityByAuthIdAndDeletedFalse(principal.authId())
                .orElseThrow(() -> {
                    logger.warn("User profile not found for update: authId={}", principal.authId());
                    return new UserProfileNotFoundException("User Profile not found");
                });
        userProfileMapper.updateFromDto(request, userProfile);
        var saved = userProfileRepository.save(userProfile);
        return userProfileMapper.toOutboundUserDto(saved, principal.email());
    }
}
