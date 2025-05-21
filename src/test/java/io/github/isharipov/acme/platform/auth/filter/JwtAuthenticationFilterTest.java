package io.github.isharipov.acme.platform.auth.filter;

import io.github.isharipov.acme.platform.common.dto.Principal;
import io.github.isharipov.acme.platform.common.exception.JwtAuthenticationException;
import io.github.isharipov.acme.platform.common.security.filter.JwtAuthenticationFilter;
import io.github.isharipov.acme.platform.common.service.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private final JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
    private final AuthenticationEntryPoint entryPoint = mock(AuthenticationEntryPoint.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenProvider, entryPoint);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldSetAuthentication_whenTokenIsValid() throws Exception {
        // GIVEN
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var filterChain = mock(FilterChain.class);

        String token = "valid.token.value";
        UUID userId = UUID.randomUUID();
        Claims claims = mock(Claims.class);
        // WHEN
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtTokenProvider.parseClaims(token)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(userId.toString());

        filter.doFilterInternal(request, response, filterChain);
        // THEN
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(userId, ((Principal) auth.getPrincipal()).authId());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldSkip_whenTokenIsMissing() throws Exception {
        // GIVEN
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var filterChain = mock(FilterChain.class);
        // WHEN
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        // THEN
        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldCallEntryPoint_whenJwtExceptionIsThrown() throws Exception {
        // GIVEN
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var filterChain = mock(FilterChain.class);

        String token = "invalid.token";

        // WHEN
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtTokenProvider.parseClaims(token)).thenThrow(new JwtAuthenticationException("Invalid token", new RuntimeException("Invalid token: " + token)));
        // THEN
        filter.doFilterInternal(request, response, filterChain);
        verify(entryPoint).commence(eq(request), eq(response), any(JwtAuthenticationException.class));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
