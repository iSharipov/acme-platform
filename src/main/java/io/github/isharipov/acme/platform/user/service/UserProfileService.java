package io.github.isharipov.acme.platform.user.service;

import io.github.isharipov.acme.platform.common.dto.Principal;
import io.github.isharipov.acme.platform.user.rest.dto.CreateUserProfileInboundDto;
import io.github.isharipov.acme.platform.user.rest.dto.UserProfileInboundDto;
import io.github.isharipov.acme.platform.user.dto.UserProfileOutboundDto;

import java.util.UUID;

public interface UserProfileService {

    UserProfileOutboundDto createUserProfile(CreateUserProfileInboundDto user);

    UserProfileOutboundDto getUserProfileByAuthId(Principal principal);

    UserProfileOutboundDto getUserById(UUID id);

    void softDeleteUserProfile(UUID authId);

    UserProfileOutboundDto createOrRestoreUserProfile(UUID authId);

    UserProfileOutboundDto updateUserProfile(Principal principal, UserProfileInboundDto user);
}
