package io.github.isharipov.acme.platform.user.service.impl;

import io.github.isharipov.acme.platform.user.dto.CreateUserProfileInboundDto;
import io.github.isharipov.acme.platform.user.dto.UserProfileOutboundDto;
import io.github.isharipov.acme.platform.user.infrastructure.UserProfileAlreadyExists;
import io.github.isharipov.acme.platform.user.infrastructure.UserProfileNotFoundException;
import io.github.isharipov.acme.platform.user.infrastructure.mapper.UserMapper;
import io.github.isharipov.acme.platform.user.repository.UserProfileRepository;
import io.github.isharipov.acme.platform.user.service.UserProfileService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserMapper userMapper;

    public UserProfileServiceImpl(UserProfileRepository userProfileRepository, UserMapper userMapper) {
        this.userProfileRepository = userProfileRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserProfileOutboundDto createUserProfile(CreateUserProfileInboundDto createUserProfile) {
        var userProfileExistsByAuthId = userProfileRepository.existsByAuthIdAndDeletedFalse(createUserProfile.authId());
        if (userProfileExistsByAuthId) {
            throw new UserProfileAlreadyExists(
                    "User Profile for the User with Id " + createUserProfile.authId() + " already exists");
        }
        return userMapper.toOutboundUserDto(
                userProfileRepository.save(userMapper.createUserToUser(createUserProfile)));
    }

    @Override
    public UserProfileOutboundDto getUserProfileByAuthId(UUID authId) {
        var userProfile = userProfileRepository.findByAuthIdAndDeletedFalse(authId)
                .orElseThrow(() -> new UserProfileNotFoundException("User Profile not found"));
        return userMapper.toOutboundUserDto(userProfile);
    }

    @Override
    public void softDeleteUserProfile(UUID authId) {
        var userProfile = userProfileRepository.findEntityByAuthIdAndDeletedFalse(authId)
                .orElseThrow(() -> new UserProfileNotFoundException("User Profile not found"));
        userProfile.setDeleted(true);
        userProfileRepository.save(userProfile);
    }

}
