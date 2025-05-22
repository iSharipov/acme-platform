package io.github.isharipov.acme.platform.common.security.filter;

import io.github.isharipov.acme.platform.common.dto.Principal;
import io.github.isharipov.acme.platform.common.exception.JwtAuthenticationException;
import io.github.isharipov.acme.platform.common.service.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, AuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request,
                                 HttpServletResponse response,
                                 FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = resolveToken(request);
            if (token != null) {
                logger.debug("Token detected in request [{}]", request.getRequestURI());
                var claims = jwtTokenProvider.parseClaims(token);
                var authId = UUID.fromString(claims.getSubject());
                var email = claims.get("email", String.class);
                logger.debug("Authenticated user authId={}, email={} from JWT", authId, email);
                var principal = new Principal(authId, email);
                var authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                logger.trace("No JWT token found in request [{}]", request.getRequestURI());
            }
            filterChain.doFilter(request, response);
        } catch (JwtAuthenticationException ex) {
            logger.debug("JWT error in filter for request [{}]: {}", request.getRequestURI(), ex.getMessage());
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, ex);
        }
    }

    private String resolveToken(HttpServletRequest request) {
        var bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken)) {
            bearerToken = bearerToken.trim();
            if (bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
            logger.debug("Authorization header present but does not start with Bearer");
        }
        return null;
    }
}