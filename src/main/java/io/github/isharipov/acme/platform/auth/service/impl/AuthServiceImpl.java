package io.github.isharipov.acme.platform.auth.service.impl;

import io.github.isharipov.acme.platform.auth.dto.*;
import io.github.isharipov.acme.platform.auth.infrastructure.UserAlreadyExistsException;
import io.github.isharipov.acme.platform.auth.infrastructure.mapper.UserAuthMapper;
import io.github.isharipov.acme.platform.auth.model.UserAuth;
import io.github.isharipov.acme.platform.auth.repository.UserAuthRepository;
import io.github.isharipov.acme.platform.auth.service.AuthService;
import io.github.isharipov.acme.platform.common.service.JwtTokenProvider;
import io.github.isharipov.acme.platform.user.dto.CreateUserProfileInboundDto;
import io.github.isharipov.acme.platform.user.service.UserProfileService;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserAuthRepository userAuthRepository;
    private final UserProfileService userService;
    private final UserAuthMapper userAuthMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserAuthRepository userAuthRepository,
                           UserProfileService userService,
                           UserAuthMapper userAuthMapper,
                           JwtTokenProvider jwtTokenProvider,
                           AuthenticationManager authenticationManager) {
        this.userAuthRepository = userAuthRepository;
        this.userService = userService;
        this.userAuthMapper = userAuthMapper;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    @Override
    public AuthOutboundDto register(RegisterInboundDto registerRequest) {
        var authUser = registerUser(registerRequest);
        userService.createUserProfile(new CreateUserProfileInboundDto(authUser.id()));
        var token = jwtTokenProvider.generateTokens(authUser.id().toString(), authUser.email());
        return new AuthOutboundDto(new UserAuthOutboundDto(authUser.email()), token);
    }

    private RegisterOutboundDto registerUser(RegisterInboundDto registerRequest) {
        var userAuthExistsByEmail = userAuthRepository.existsByEmail(registerRequest.email());
        if (userAuthExistsByEmail) {
            throw new UserAlreadyExistsException(
                    "User with email " + registerRequest.email() + " already exists");
        }
        return userAuthMapper.toRegisterOutbound(userAuthRepository.save(userAuthMapper.toUserAuth(registerRequest)));
    }

    @Override
    public AuthOutboundDto login(AuthInboundDto authRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.login(), authRequest.password())
        );
        var userAuth = userAuthRepository.findByEmail(authRequest.login())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        var token = jwtTokenProvider.generateTokens(userAuth.getId().toString(), userAuth.getEmail());

        return new AuthOutboundDto(new UserAuthOutboundDto(userAuth.getEmail()), token);
    }

    @Transactional
    @Override
    public void deleteSelf(UUID authId) {
        var authUser = userAuthRepository.findById(authId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        userService.softDeleteUserProfile(authId);
        authUser.setStatus(UserAuth.UserStatus.DELETED);
        userAuthRepository.save(authUser);
    }
}
