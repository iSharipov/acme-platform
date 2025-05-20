package io.github.isharipov.acme.platform.user.service.impl;

import io.github.isharipov.acme.platform.user.dto.CreateUserProfileInboundDto;
import io.github.isharipov.acme.platform.user.dto.UserProfileOutboundDto;
import io.github.isharipov.acme.platform.user.infrastructure.UserProfileAlreadyExists;
import io.github.isharipov.acme.platform.user.infrastructure.UserProfileNotFoundException;
import io.github.isharipov.acme.platform.user.infrastructure.mapper.UserMapper;
import io.github.isharipov.acme.platform.user.repository.UserProfileRepository;
import io.github.isharipov.acme.platform.user.service.UserProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileServiceImpl.class);

    private final UserProfileRepository userProfileRepository;
    private final UserMapper userMapper;

    public UserProfileServiceImpl(UserProfileRepository userProfileRepository, UserMapper userMapper) {
        this.userProfileRepository = userProfileRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserProfileOutboundDto createUserProfile(CreateUserProfileInboundDto createUserProfile) {
        logger.info("Creating user profile for authId={}", createUserProfile.authId());
        var userProfileExistsByAuthId = userProfileRepository.existsByAuthIdAndDeletedFalse(createUserProfile.authId());
        if (userProfileExistsByAuthId) {
            logger.warn("User profile already exists for authId={}", createUserProfile.authId());
            throw new UserProfileAlreadyExists(
                    "User Profile for the User with Id " + createUserProfile.authId() + " already exists");
        }
        var saved = userProfileRepository.save(userMapper.createUserToUser(createUserProfile));
        logger.info("User profile created: id={}, authId={}", saved.getId(), saved.getAuthId());
        return userMapper.toOutboundUserDto(saved);
    }

    @Override
    public UserProfileOutboundDto getUserProfileByAuthId(UUID authId) {
        logger.info("Fetching user profile by authId={}", authId);
        var userProfile = userProfileRepository.findByAuthIdAndDeletedFalse(authId)
                .orElseThrow(() -> {
                    logger.warn("User profile not found for authId={}", authId);
                    return new UserProfileNotFoundException("User Profile not found");
                });
        return userMapper.toOutboundUserDto(userProfile);
    }

    @Override
    public UserProfileOutboundDto getUserById(UUID id) {
        logger.info("Fetching user profile by id={}", id);
        var userProfile = userProfileRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> {
                    logger.warn("User profile not found for id={}", id);
                    return new UserProfileNotFoundException("User Profile not found");
                });
        return userMapper.toOutboundUserDto(userProfile);
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
}
