package io.github.isharipov.acme.platform.user.service.impl;

import io.github.isharipov.acme.platform.user.dto.CreateUserProfileInboundDto;
import io.github.isharipov.acme.platform.user.dto.UserProfileOutboundDto;
import io.github.isharipov.acme.platform.user.infrastructure.UserProfileAlreadyExists;
import io.github.isharipov.acme.platform.user.infrastructure.UserProfileNotFoundException;
import io.github.isharipov.acme.platform.user.infrastructure.mapper.UserMapper;
import io.github.isharipov.acme.platform.user.repository.UserRepository;
import io.github.isharipov.acme.platform.user.service.UserProfileService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserProfileServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserProfileOutboundDto createUserProfile(CreateUserProfileInboundDto createUserProfile) {
        var userProfileExistsByAuthId = userRepository.existsByAuthId(createUserProfile.authId());
        if (userProfileExistsByAuthId) {
            throw new UserProfileAlreadyExists(
                    "User Profile for the User with Id " + createUserProfile.authId() + " already exists");
        }
        return userMapper.toOutboundUserDto(
                userRepository.save(userMapper.createUserToUser(createUserProfile)));
    }

    @Override
    public UserProfileOutboundDto getUserProfileByAuthId(UUID authId) {
        var userProfile = userRepository.findByAuthId(authId)
                .orElseThrow(() -> new UserProfileNotFoundException("User Profile not found"));
        return userMapper.toOutboundUserDto(userProfile);
    }

}
