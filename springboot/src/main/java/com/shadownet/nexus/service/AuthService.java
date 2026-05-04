package com.shadownet.nexus.service;

import com.shadownet.nexus.entity.EmailVerificationToken;
import com.shadownet.nexus.entity.PasswordResetToken;
import com.shadownet.nexus.entity.User;
import com.shadownet.nexus.repository.EmailVerificationTokenRepository;
import com.shadownet.nexus.repository.PasswordResetTokenRepository;
import com.shadownet.nexus.repository.UserRepository;
import com.shadownet.nexus.util.AuthenticationAuditLogger;
import com.shadownet.nexus.util.InputValidator;
import com.shadownet.nexus.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final long EMAIL_VERIFICATION_TOKEN_EXPIRY = 24 * 60 * 60 * 1000;
    private static final long PASSWORD_RESET_TOKEN_EXPIRY = 60 * 60 * 1000;
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final long ACCOUNT_LOCKOUT_WINDOW_MS = 15 * 60 * 1000;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationAuditLogger auditLogger;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Environment environment;

    @Value("${app.security.email-encryption-key:}")
    private String emailEncryptionKey;

    @Value("${app.security.require-email-verification:false}")
    private boolean requireEmailVerification;

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @PostConstruct
    public void validateEncryptionKey() {
        if (emailEncryptionKey == null || emailEncryptionKey.isBlank()) {
            throw new IllegalStateException(
                    "EMAIL_ENCRYPTION_KEY environment variable must be set before application startup.");
        }
        if (emailEncryptionKey.getBytes(StandardCharsets.UTF_8).length < 16) {
            throw new IllegalStateException("EMAIL_ENCRYPTION_KEY must be at least 16 bytes long");
        }
    }

    public User register(String email, String username, String password, String ipAddress) {
        try {
            String normalizedEmail = normalizeEmail(email);
            String normalizedUsername = username == null ? null : username.trim();

            validateRegistrationInput(normalizedEmail, normalizedUsername, password, ipAddress);

            String emailHash = hashEmail(normalizedEmail);
            if (userRepository.findByEmailHash(emailHash) != null) {
                auditLogger.logFailedRegistration(normalizedEmail, ipAddress, "User already exists");
                throw new RuntimeException("User already exists");
            }

            User user = new User();
            user.setId("user_" + UUID.randomUUID().toString().replace("-", ""));
            user.setEmail(normalizedEmail);
            user.setEmailHash(emailHash);
            user.setEmailEncrypted(encryptEmail(normalizedEmail));
            user.setUsername(normalizedUsername);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setDisplayName(normalizedUsername);
            user.setCreatedAt(System.currentTimeMillis());
            user.setUpdatedAt(System.currentTimeMillis());
            user.setEmailVerified(false);
            user.setAccountLocked(false);
            user.setFailedLoginAttempts(0);
            user.setScore(0);
            user.setXp(0);
            user.setLevel(1);

            userRepository.save(user);
            if (isEmailVerificationRequired()) {
                generateEmailVerificationToken(user);
            }
            auditLogger.logSuccessfulRegistration(user.getId(), normalizedEmail, ipAddress);
            logger.info("User registered: {}", user.getId());
            return user;
        } catch (Exception e) {
            logger.error("Registration error: {}", e.getMessage());
            throw e;
        }
    }

    public String login(String email, String password, String ipAddress) {
        try {
            String normalizedEmail = normalizeEmail(email);

            if (!InputValidator.isValidEmail(normalizedEmail)) {
                auditLogger.logFailedLogin(normalizedEmail, ipAddress, "Invalid email format");
                throw new IllegalArgumentException("Invalid credentials");
            }

            if (InputValidator.containsSqlInjectionAttempt(normalizedEmail)) {
                auditLogger.logSuspiciousInput(null, "/api/login", "SQL_INJECTION", ipAddress);
                throw new SecurityException("Invalid input detected");
            }

            if (auditLogger.isIpLocked(ipAddress)) {
                auditLogger.logFailedLogin(normalizedEmail, ipAddress, "IP locked - brute force protection");
                throw new RuntimeException("Too many failed login attempts. Please try again later.");
            }

            String emailHash = hashEmail(normalizedEmail);
            User user = userRepository.findByEmailHash(emailHash);
            if (user == null) {
                auditLogger.logFailedLogin(normalizedEmail, ipAddress, "Invalid credentials");
                throw new RuntimeException("Invalid credentials");
            }

            if (isUserLocked(user)) {
                auditLogger.logFailedLogin(normalizedEmail, ipAddress, "Account locked");
                throw new RuntimeException("Account is locked. Please try again later.");
            }

            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                registerFailedLogin(user, ipAddress, "Invalid credentials");
                throw new RuntimeException("Invalid credentials");
            }

            if (isEmailVerificationRequired() && !Boolean.TRUE.equals(user.getEmailVerified())) {
                auditLogger.logFailedLogin(normalizedEmail, ipAddress, "Email not verified");
                throw new RuntimeException("Verify your email before logging in.");
            }

            user.setFailedLoginAttempts(0);
            user.setLastFailedLoginAt(null);
            user.setLockedUntil(null);
            user.setAccountLocked(false);
            user.setLastLoginAt(System.currentTimeMillis());
            user.setUpdatedAt(System.currentTimeMillis());
            userRepository.save(user);

            auditLogger.logSuccessfulLogin(user.getId(), normalizedEmail, ipAddress);
            logger.info("User logged in: {}", user.getId());
            return jwtUtil.generateToken(user.getId());
        } catch (Exception e) {
            logger.error("Login error: {}", e.getMessage());
            throw e;
        }
    }

    public void requestPasswordReset(String email, String ipAddress) {
        String normalizedEmail = normalizeEmail(email);
        if (!InputValidator.isValidEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Invalid email");
        }

        String emailHash = hashEmail(normalizedEmail);
        User user = userRepository.findByEmailHash(emailHash);
        if (user == null) {
            auditLogger.logFailedRegistration(normalizedEmail, ipAddress, "Password reset request for non-existent user");
            return;
        }

        generatePasswordResetToken(user);
        logger.info("Password reset token generated for user: {}", user.getId());
    }

    public void resetPassword(String token, String newPassword, String ipAddress) {
        if (!InputValidator.isValidPassword(newPassword)) {
            throw new IllegalArgumentException("Password does not meet security requirements");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(hashToken(token));
        if (resetToken == null || resetToken.isExpired() || Boolean.TRUE.equals(resetToken.getUsed())) {
            auditLogger.logFailedLogin(null, ipAddress, "Invalid or expired password reset token");
            throw new RuntimeException("Invalid or expired password reset token");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        user.setLastFailedLoginAt(null);
        user.setLockedUntil(null);
        user.setAccountLocked(false);
        user.setUpdatedAt(System.currentTimeMillis());
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        passwordResetTokenRepository.deleteByUser_Id(user.getId());
        auditLogger.logSuccessfulLogin(user.getId(), user.getEmail(), ipAddress);
        logger.info("Password reset successful for user: {}", user.getId());
    }

    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByTokenHash(hashToken(token));
        if (verificationToken == null || verificationToken.isExpired() || Boolean.TRUE.equals(verificationToken.getVerified())) {
            throw new RuntimeException("Invalid or expired email verification token");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        user.setUpdatedAt(System.currentTimeMillis());
        userRepository.save(user);

        verificationToken.setVerified(true);
        emailVerificationTokenRepository.save(verificationToken);
        emailVerificationTokenRepository.deleteByUser_Id(user.getId());
        logger.info("Email verified for user: {}", user.getId());
    }

    public boolean isEmailVerificationRequired() {
        if (!requireEmailVerification) {
            return false;
        }

        String url = datasourceUrl == null ? "" : datasourceUrl.toLowerCase();
        if (url.contains(":h2:")) {
            return false;
        }

        for (String profile : environment.getActiveProfiles()) {
            if ("local".equalsIgnoreCase(profile) || "dev".equalsIgnoreCase(profile)) {
                return false;
            }
        }

        return true;
    }

    private void validateRegistrationInput(String email, String username, String password, String ipAddress) {
        if (!InputValidator.isValidEmail(email)) {
            auditLogger.logFailedRegistration(email, ipAddress, "Invalid email format");
            throw new IllegalArgumentException("Invalid email format");
        }
        if (!InputValidator.isValidUsername(username)) {
            auditLogger.logFailedRegistration(email, ipAddress, "Invalid username format");
            throw new IllegalArgumentException("Username must be 3-32 characters, alphanumeric with _ and -");
        }
        if (!InputValidator.isValidPassword(password)) {
            auditLogger.logFailedRegistration(email, ipAddress, "Weak password");
            throw new IllegalArgumentException(
                    "Password must be 8+ characters with uppercase, lowercase, digit, and special character");
        }
        if (InputValidator.containsSqlInjectionAttempt(email)
                || InputValidator.containsSqlInjectionAttempt(username)
                || InputValidator.containsXssAttempt(username)) {
            auditLogger.logFailedRegistration(email, ipAddress, "Invalid input detected");
            throw new SecurityException("Invalid input detected");
        }
    }

    private void generatePasswordResetToken(User user) {
        passwordResetTokenRepository.deleteByUser_Id(user.getId());
        PasswordResetToken resetToken = new PasswordResetToken(
                "prt_" + UUID.randomUUID().toString().replace("-", ""),
                user,
                hashToken(generateSecureToken()),
                System.currentTimeMillis() + PASSWORD_RESET_TOKEN_EXPIRY);
        passwordResetTokenRepository.save(resetToken);
    }

    private void generateEmailVerificationToken(User user) {
        emailVerificationTokenRepository.deleteByUser_Id(user.getId());
        EmailVerificationToken verifyToken = new EmailVerificationToken(
                "evt_" + UUID.randomUUID().toString().replace("-", ""),
                user,
                hashToken(generateSecureToken()),
                System.currentTimeMillis() + EMAIL_VERIFICATION_TOKEN_EXPIRY);
        emailVerificationTokenRepository.save(verifyToken);
    }

    private boolean isUserLocked(User user) {
        Long lockedUntil = user.getLockedUntil();
        if (lockedUntil == null) {
            return Boolean.TRUE.equals(user.getAccountLocked());
        }
        if (lockedUntil <= System.currentTimeMillis()) {
            user.setAccountLocked(false);
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
            return false;
        }
        return true;
    }

    private void registerFailedLogin(User user, String ipAddress, String reason) {
        int attempts = user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts();
        attempts += 1;
        user.setFailedLoginAttempts(attempts);
        user.setLastFailedLoginAt(System.currentTimeMillis());
        if (attempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
            user.setAccountLocked(true);
            user.setLockedUntil(System.currentTimeMillis() + ACCOUNT_LOCKOUT_WINDOW_MS);
        }
        user.setUpdatedAt(System.currentTimeMillis());
        userRepository.save(user);
        auditLogger.logFailedLogin(user.getEmail(), ipAddress, reason);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String hashEmail(String email) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(email.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String encryptEmail(String email) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec key = new SecretKeySpec(emailEncryptionKey.getBytes(StandardCharsets.UTF_8), 0, 16, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedData = cipher.doFinal(email.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            throw new IllegalStateException("Email encryption failed", e);
        }
    }

    private String hashToken(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(token.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
