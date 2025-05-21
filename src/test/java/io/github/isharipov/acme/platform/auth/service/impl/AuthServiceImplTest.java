package io.github.isharipov.acme.platform.auth.service.impl;

import io.github.isharipov.acme.platform.auth.rest.dto.AuthInboundDto;
import io.github.isharipov.acme.platform.auth.infrastructure.mapper.UserAuthMapper;
import io.github.isharipov.acme.platform.auth.model.UserAuth;
import io.github.isharipov.acme.platform.auth.repository.UserAuthRepository;
import io.github.isharipov.acme.platform.common.exception.JwtAuthenticationException;
import io.github.isharipov.acme.platform.common.exception.RefreshTokenMismatchException;
import io.github.isharipov.acme.platform.common.service.JwtTokenProvider;
import io.github.isharipov.acme.platform.user.service.UserProfileService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserAuthRepository userAuthRepository;
    @Mock
    private UserProfileService userService;
    @Mock
    private UserAuthMapper userAuthMapper;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private final UUID userId = UUID.randomUUID();
    private final String email = "test@example.com";
    private final String refreshToken = "refresh-token";

    @Test
    void login_shouldThrow_whenUserNotFound() {
        // GIVEN
        var loginDto = new AuthInboundDto(email, "password");
        // WHEN
        when(userAuthRepository.findByEmail(email)).thenReturn(Optional.empty());
        // THEN
        assertThrows(UsernameNotFoundException.class, () -> authService.login(loginDto));
    }

    @Test
    void deleteSelf_shouldThrow_whenUserNotFound() {
        // GIVEN
        // WHEN
        when(userAuthRepository.findById(userId)).thenReturn(Optional.empty());
        // THEN
        assertThrows(UsernameNotFoundException.class, () -> authService.deleteSelf(userId));
    }

    @Test
    void refreshToken_shouldThrow_whenJwtInvalid() {
        // GIVEN
        // WHEN
        when(jwtTokenProvider.parseClaims(refreshToken)).thenThrow(new JwtAuthenticationException("Invalid", new RuntimeException(refreshToken)));
        // THEN
        assertThrows(JwtAuthenticationException.class, () -> authService.refreshToken(refreshToken));
    }

    @Test
    void refreshToken_shouldThrow_whenUserNotFound() {
        // GIVEN
        var claims = mock(Claims.class);
        // WHEN
        when(claims.getSubject()).thenReturn(userId.toString());
        when(jwtTokenProvider.parseClaims(refreshToken)).thenReturn(claims);
        when(userAuthRepository.findById(userId)).thenReturn(Optional.empty());
        // THEN
        assertThrows(UsernameNotFoundException.class, () -> authService.refreshToken(refreshToken));
    }

    @Test
    void refreshToken_shouldThrow_whenTokenMismatch() {
        // GIVEN
        var claims = mock(Claims.class);
        var userAuth = new UserAuth();
        userAuth.setId(userId);
        userAuth.setEmail(email);
        userAuth.setRefreshToken("other-token");
        // WHEN
        when(claims.getSubject()).thenReturn(userId.toString());
        when(jwtTokenProvider.parseClaims(refreshToken)).thenReturn(claims);
        when(userAuthRepository.findById(userId)).thenReturn(Optional.of(userAuth));
        // THEN
        assertThrows(RefreshTokenMismatchException.class, () -> authService.refreshToken(refreshToken));
    }
}