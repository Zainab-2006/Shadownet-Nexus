package com.shadownet.nexus.security;

import com.shadownet.nexus.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {
    
    private final RateLimitService rateLimitService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) 
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        String clientId = getClientIdentifier(request);
        
        RateLimitType limitType = getRateLimitType(path);
        
        if (limitType != null && !rateLimitService.isAllowed(clientId, limitType)) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too many requests. Please try again later.\"}");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getClientIdentifier(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return "user:" + extractUsernameFromToken(authHeader.substring(7));
        }
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null) {
            return "ip:" + xForwardedFor.split(",")[0];
        }
        return "ip:" + request.getRemoteAddr();
    }
    
    private RateLimitType getRateLimitType(String path) {
        if (path.equals("/api/login") || path.equals("/api/register") || path.equals("/api/auth/login") || path.equals("/api/auth/register") || path.equals("/api/request-password-reset") || path.equals("/api/reset-password")) {
            return RateLimitType.AUTH;
        }
        if (path.equals("/api/submit-flag") || path.equals("/api/puzzle/submit") || path.equals("/api/pcg/solo/submit")) {
            return RateLimitType.CHALLENGE_SUBMIT;
        }
        if (path.equals("/api/pcg/solo/generate")) {
            return RateLimitType.PCG_GENERATE;
        }
        if (path.equals("/api/puzzle/hint")) {
            return RateLimitType.HINT;
        }
        if (path.matches("^/api/challenges/[^/]+/spawn$")) {
            return RateLimitType.CONTAINER_SPAWN;
        }
        if (path.equals("/api/team") || path.startsWith("/api/team/")) {
            return RateLimitType.TEAM_ACTION;
        }
        return null;
    }
    
    private String extractUsernameFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
                int idx = payload.indexOf("\"sub\":\"");
                if (idx > 0) {
                    int start = idx + 7;
                    int end = payload.indexOf("\"", start);
                    if (end > start) {
                        return payload.substring(start, end);
                    }
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return "unknown";
    }
    
    public enum RateLimitType {
        AUTH(5, 900),           
        CHALLENGE_SUBMIT(10, 300), 
        HINT(3, 300),           
        TEAM_ACTION(20, 60),
        CONTAINER_SPAWN(5, 300),
        PCG_GENERATE(10, 300);
    
        public final int maxAttempts;
        public final int windowSeconds;
        
        RateLimitType(int maxAttempts, int windowSeconds) {
            this.maxAttempts = maxAttempts;
            this.windowSeconds = windowSeconds;
        }
    }
}

