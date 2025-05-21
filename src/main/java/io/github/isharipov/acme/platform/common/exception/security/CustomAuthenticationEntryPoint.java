package io.github.isharipov.acme.platform.common.exception.security;

import io.github.isharipov.acme.platform.common.exception.model.ErrorType;
import io.github.isharipov.acme.platform.common.util.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.hc.core5.http.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        logger.warn("Authentication failed: {}", authException.getMessage());
        var error = ErrorType.AUTH_ERROR.getErrorResponse(authException.getMessage());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        response.getWriter().write(JsonUtil.toJson(error.getBody()));
    }
}