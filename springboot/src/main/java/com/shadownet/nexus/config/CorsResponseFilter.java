package com.shadownet.nexus.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsResponseFilter extends OncePerRequestFilter {

    private final List<String> allowedOrigins;
    private static final String DEFAULT_ALLOWED_HEADERS =
            "Authorization,Content-Type,Accept,Origin,X-Requested-With,Cache-Control,Pragma";

    public CorsResponseFilter(
            @Value("${spring.web.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173,http://localhost:3000,http://localhost:8080,http://127.0.0.1:8080,http://localhost:8081,https://shadownet-frontend.onrender.com,https://shadownet-nexus.vercel.app}") String allowedOrigins) {
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String origin = request.getHeader(HttpHeaders.ORIGIN);

        if (origin != null && isAllowedOrigin(origin)) {
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,DELETE,OPTIONS");
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, allowedHeaders(request));
            response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Retry-After");
            response.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
            response.addHeader(HttpHeaders.VARY, HttpHeaders.ORIGIN);
            response.addHeader(HttpHeaders.VARY, HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD);
            response.addHeader(HttpHeaders.VARY, HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
        }

        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowedOrigin(String origin) {
        return allowedOrigins.stream().anyMatch(allowedOrigin -> allowedOrigin.equals(origin)
                || PatternMatchUtils.simpleMatch(allowedOrigin, origin));
    }

    private String allowedHeaders(HttpServletRequest request) {
        String requestedHeaders = request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
        if (requestedHeaders == null || requestedHeaders.isBlank()) {
            return DEFAULT_ALLOWED_HEADERS;
        }
        return requestedHeaders;
    }
}
