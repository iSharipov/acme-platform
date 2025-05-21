package io.github.isharipov.acme.platform.auth.service.impl;

import io.github.isharipov.acme.platform.auth.infrastructure.exception.UserAlreadyExistsException;
import io.github.isharipov.acme.platform.auth.infrastructure.mapper.UserAuthMapper;
import io.github.isharipov.acme.platform.auth.model.UserAuth;
import io.github.isharipov.acme.platform.auth.repository.UserAuthRepository;
import io.github.isharipov.acme.platform.auth.rest.dto.AuthInboundDto;
import io.github.isharipov.acme.platform.auth.rest.dto.AuthOutboundDto;
import io.github.isharipov.acme.platform.auth.rest.dto.RegisterInboundDto;
import io.github.isharipov.acme.platform.auth.rest.dto.UserAuthOutboundDto;
import io.github.isharipov.acme.platform.auth.service.AuthService;
import io.github.isharipov.acme.platform.common.dto.TokenOutboundDto;
import io.github.isharipov.acme.platform.common.exception.JwtAuthenticationException;
import io.github.isharipov.acme.platform.common.exception.RefreshTokenMismatchException;
import io.github.isharipov.acme.platform.common.service.JwtTokenProvider;
import io.github.isharipov.acme.platform.user.rest.dto.CreateUserProfileInboundDto;
import io.github.isharipov.acme.platform.user.service.UserProfileService;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

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
        logger.info("Attempting to register user with email={}", registerRequest.email());
        var authUser = registerUser(registerRequest);
        logger.info("Successfully registered user with id={} and email={}", authUser.getId(), authUser.getEmail());
        userService.createUserProfile(new CreateUserProfileInboundDto(authUser.getId()));
        var token = jwtTokenProvider.generateTokens(authUser.getId().toString(), authUser.getEmail());
        authUser.setRefreshToken(token.refreshToken());
        userAuthRepository.save(authUser);
        return new AuthOutboundDto(new UserAuthOutboundDto(authUser.getEmail()), token);
    }

    private UserAuth registerUser(RegisterInboundDto registerRequest) {
        var existingUser = userAuthRepository.findByEmail(registerRequest.email());
        if (existingUser.isPresent()) {
            var user = existingUser.get();
            if (user.getStatus() != UserAuth.UserStatus.DELETED) {
                logger.warn("Attempt to register already existing user with email={}", registerRequest.email());
                throw new UserAlreadyExistsException(
                        "User with email " + registerRequest.email() + " already exists");
            }

            user.setPassword(registerRequest.password());
            user.setStatus(UserAuth.UserStatus.ACTIVE);
            return userAuthRepository.save(user);
        }
        return userAuthRepository.save(userAuthMapper.toUserAuth(registerRequest));
    }

    @Override
    public AuthOutboundDto login(AuthInboundDto authRequest) {
        logger.info("Login attempt for email={}", authRequest.login());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.login(), authRequest.password())
        );
        var userAuth = userAuthRepository.findByEmail(authRequest.login())
                .orElseThrow(() -> {
                    logger.warn("Login failed: user not found for email={}", authRequest.login());
                    return new UsernameNotFoundException("User not found");
                });
        logger.info("User login successful: id={}, email={}", userAuth.getId(), userAuth.getEmail());
        var token = jwtTokenProvider.generateTokens(userAuth.getId().toString(), userAuth.getEmail());
        userAuth.setRefreshToken(token.refreshToken());
        userAuthRepository.save(userAuth);
        return new AuthOutboundDto(new UserAuthOutboundDto(userAuth.getEmail()), token);
    }

    @Transactional
    @Override
    public void deleteSelf(UUID authId) {
        logger.info("Deletion request received for user id={}", authId);
        var authUser = userAuthRepository.findById(authId)
                .orElseThrow(() -> {
                    logger.warn("Deletion failed: user not found for id={}", authId);
                    return new UsernameNotFoundException("User not found");
                });
        userService.softDeleteUserProfile(authId);
        authUser.setStatus(UserAuth.UserStatus.DELETED);
        userAuthRepository.save(authUser);

        logger.info("User account marked as deleted: id={}, email={}", authUser.getId(), authUser.getEmail());
    }

    @Override
    public TokenOutboundDto refreshToken(String refreshToken) {
        logger.info("Received refresh token request");
        Claims claims;
        try {
            claims = jwtTokenProvider.parseClaims(refreshToken);
            logger.debug("Parsed refresh token successfully for subject={}", claims.getSubject());
        } catch (JwtAuthenticationException e) {
            logger.warn("Failed to parse refresh token: {}", e.getMessage());
            throw e;
        }
        var userId = UUID.fromString(claims.getSubject());
        logger.debug("Extracted userId from token: {}", userId);
        var user = userAuthRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found for ID: {}", userId);
                    return new UsernameNotFoundException("User not found");
                });

        if (!refreshToken.equals(user.getRefreshToken())) {
            logger.warn("Refresh token mismatch for userId={} and email={}", userId, user.getEmail());
            throw new RefreshTokenMismatchException("Refresh token mismatch");
        }

        logger.info("Refresh token valid for userId={}, issuing new tokens", userId);
        var tokens = jwtTokenProvider.generateTokens(userId.toString(), user.getEmail());

        user.setRefreshToken(tokens.refreshToken());
        userAuthRepository.save(user);
        logger.debug("Stored new refresh token for userId={}", userId);

        return new TokenOutboundDto(tokens.accessToken(), tokens.refreshToken());
    }
}
