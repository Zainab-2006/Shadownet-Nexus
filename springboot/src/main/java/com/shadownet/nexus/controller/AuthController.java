package com.shadownet.nexus.controller;

import com.shadownet.nexus.config.SecurityConfig;
import com.shadownet.nexus.dto.AuthResponse;
import com.shadownet.nexus.dto.ErrorResponse;
import com.shadownet.nexus.dto.LoginRequest;
import com.shadownet.nexus.dto.RegisterRequest;
import com.shadownet.nexus.entity.User;
import com.shadownet.nexus.repository.UserRepository;
import com.shadownet.nexus.service.AuthService;
import com.shadownet.nexus.util.JwtUtil;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIpAddress(httpRequest);
            Bucket bucket = SecurityConfig.getRateLimitingBucket(ipAddress + ":register");
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            if (!probe.isConsumed()) {
                logger.warn("Rate limit exceeded for registration from IP: {}", ipAddress);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(new ErrorResponse("RATE_LIMIT_EXCEEDED", "Too many registration attempts. Please try again later.", 429));
            }

            String token = authService.register(request.getEmail(), request.getUsername(), request.getPassword(), ipAddress);
            logger.info("User registered successfully from IP: {}", ipAddress);
            return ResponseEntity.status(HttpStatus.CREATED).body(buildAuthResponse(token));
        } catch (SecurityException e) {
            logger.error("Security violation in registration: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("SECURITY_VIOLATION", "Invalid input detected", 403));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid registration input: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_INPUT", e.getMessage(), 400));
        } catch (RuntimeException e) {
            logger.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse("REGISTRATION_FAILED", e.getMessage(), 400));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIpAddress(httpRequest);
            Bucket bucket = SecurityConfig.getRateLimitingBucket(ipAddress + ":login");
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            if (!probe.isConsumed()) {
                logger.warn("Rate limit exceeded for login from IP: {}", ipAddress);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(new ErrorResponse("RATE_LIMIT_EXCEEDED", "Too many login attempts. Please try again later.", 429));
            }

            String token = authService.login(request.getEmail(), request.getPassword(), ipAddress);
            logger.info("User logged in successfully from IP: {}", ipAddress);
            return ResponseEntity.ok(buildAuthResponse(token));
        } catch (SecurityException e) {
            logger.error("Security violation in login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("SECURITY_VIOLATION", "Access denied", 403));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid login input: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("INVALID_CREDENTIALS", "Invalid email or password", 401));
        } catch (RuntimeException e) {
            logger.warn("Login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("LOGIN_FAILED", e.getMessage(), 401));
        }
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@RequestParam String email, HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIpAddress(httpRequest);
            Bucket bucket = SecurityConfig.getRateLimitingBucket(ipAddress + ":password-reset");
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            if (!probe.isConsumed()) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(new ErrorResponse("RATE_LIMIT_EXCEEDED", "Too many reset requests. Please try again later.", 429));
            }

            authService.requestPasswordReset(email, ipAddress);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset link sent to your email");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Password reset request failed: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset link sent to your email");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword, HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIpAddress(httpRequest);
            authService.resetPassword(token, newPassword, ipAddress);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_PASSWORD", e.getMessage(), 400));
        } catch (RuntimeException e) {
            logger.warn("Password reset failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("INVALID_TOKEN", e.getMessage(), 401));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            authService.verifyEmail(token);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email verified successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("Email verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("INVALID_TOKEN", e.getMessage(), 401));
        }
    }

    private AuthResponse buildAuthResponse(String token) {
        AuthResponse response = new AuthResponse();
        response.setToken(token);

        try {
            String userId = jwtUtil.extractUserId(token);
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                response.setUser(new AuthResponse.AuthUser(user.getId(), user.getUsername(), user.getDisplayName()));
            }
        } catch (Exception e) {
            logger.warn("Unable to enrich auth response: {}", e.getMessage());
        }

        return response;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
