package io.github.isharipov.acme.platform.user.service;

import io.github.isharipov.acme.platform.user.dto.CreateUserProfileInboundDto;
import io.github.isharipov.acme.platform.user.dto.UserProfileOutboundDto;

import java.util.UUID;

public interface UserProfileService {

    UserProfileOutboundDto createUserProfile(CreateUserProfileInboundDto user);

    UserProfileOutboundDto getUserProfileByAuthId(UUID authId);
}
