package io.github.isharipov.acme.platform.common.exception.model;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.UrlUtils;

import java.util.Enumeration;

public class HttpServletRequestLog {

    private final HttpServletRequest request;
    private final Object body;

    public HttpServletRequestLog(HttpServletRequest request) {
        this(request, null);
    }

    public HttpServletRequestLog(HttpServletRequest request, Object body) {
        this.request = request;
        this.body = body;
    }

    @Override
    public String toString() {
        StringBuilder headers = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headerValues = request.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                headers.append(headerName)
                        .append(" = ")
                        .append(headerValues.nextElement())
                        .append("\n");
            }
        }

        String user;
        try {
            user = String.valueOf(getPrincipal());
        } catch (RuntimeException e) {
            user = null;
        }

        return String.format("""
                        Request uri: %s %s
                        Auth: %s
                        Request headers:
                        %sRequest body length: %d
                        Body: %s
                        """,
                request.getMethod(),
                UrlUtils.buildRequestUrl(request),
                user,
                headers,
                request.getContentLengthLong(),
                body != null ? body : "null or unknown"
        );
    }

    private Object getPrincipal() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } else {
            return null;
        }
    }
}