package io.github.isharipov.acme.platform.common.service;

import io.github.isharipov.acme.platform.auth.dto.TokenOutboundDto;
import io.github.isharipov.acme.platform.common.exception.JwtAuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    public String generateAccessToken(String subject, String email) {
        return Jwts.builder()
                .subject(subject)
                .claim("email", email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(String subject, String email) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();
    }

    public TokenOutboundDto generateTokens(String subject, String email) {
        logger.debug("Generating JWT tokens for subject={}, email={}", subject, email);
        var accessToken = generateAccessToken(subject, email);
        var refreshToken = generateRefreshToken(subject, email);
        return new TokenOutboundDto(accessToken, refreshToken);
    }

    public Claims parseClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            logger.debug("Successfully parsed claims from token");
            return claims;
        } catch (JwtException | IllegalArgumentException ex) {
            logger.warn("Failed to parse JWT claims: {}", ex.getMessage());
            throw new JwtAuthenticationException("Invalid or expired JWT token", ex);
        }
    }

}