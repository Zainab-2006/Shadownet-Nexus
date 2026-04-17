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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

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

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    private static final long EMAIL_VERIFICATION_TOKEN_EXPIRY = 24 * 60 * 60 * 1000;
    private static final long PASSWORD_RESET_TOKEN_EXPIRY = 60 * 60 * 1000;
    private static final String AES_ENCRYPTION_KEY = System.getenv("EMAIL_ENCRYPTION_KEY");

    public String register(String email, String username, String password, String ipAddress) {
        try {
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
                    || InputValidator.containsSqlInjectionAttempt(password)) {
                auditLogger.logFailedRegistration(email, ipAddress, "SQL injection attempt detected");
                throw new SecurityException("Invalid input detected");
            }

            String emailHash = hashEmail(email);
            if (userRepository.findByEmailHash(emailHash) != null) {
                auditLogger.logFailedRegistration(email, ipAddress, "User already exists");
                throw new RuntimeException("User already exists");
            }

            User user = new User();
            user.setId("user_" + UUID.randomUUID().toString().replace("-", ""));
            user.setEmail(email);
            user.setEmailHash(emailHash);
            user.setEmailEncrypted(encryptEmail(email));
            user.setUsername(username);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setDisplayName(username);
            user.setCreatedAt(System.currentTimeMillis());
            user.setUpdatedAt(System.currentTimeMillis());
            user.setEmailVerified(false);
            user.setAccountLocked(false);
            user.setFailedLoginAttempts(0);
            user.setScore(0);
            user.setXp(0);
            user.setLevel(1);

            userRepository.save(user);
                        generateEmailVerificationToken(user);
            auditLogger.logSuccessfulRegistration(user.getId(), email, ipAddress);
            logger.info("User registered: {}", user.getId());
            return jwtUtil.generateToken(user.getId());
        } catch (Exception e) {
            logger.error("Registration error: {}", e.getMessage());
            throw e;
        }
    }

    public String login(String email, String password, String ipAddress) {
        try {
            if (!InputValidator.isValidEmail(email)) {
                auditLogger.logFailedLogin(email, ipAddress, "Invalid email format");
                throw new IllegalArgumentException("Invalid credentials");
            }

            if (InputValidator.containsSqlInjectionAttempt(email)
                    || InputValidator.containsSqlInjectionAttempt(password)) {
                auditLogger.logSuspiciousInput(null, "/api/login", "SQL_INJECTION", ipAddress);
                throw new SecurityException("Invalid input detected");
            }

            if (auditLogger.isIpLocked(ipAddress)) {
                auditLogger.logFailedLogin(email, ipAddress, "IP locked - brute force protection");
                throw new RuntimeException("Too many failed login attempts. Please try again later.");
            }

            String emailHash = hashEmail(email);
            User user = userRepository.findByEmailHash(emailHash);

            if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
                auditLogger.logFailedLogin(email, ipAddress, "Invalid credentials");
                throw new RuntimeException("Invalid credentials");
            }

            if (Boolean.TRUE.equals(user.getAccountLocked())) {
                auditLogger.logFailedLogin(email, ipAddress, "Account locked");
                throw new RuntimeException("Account is locked. Contact support.");
            }

            user.setFailedLoginAttempts(0);
            user.setLastLoginAt(System.currentTimeMillis());
            user.setUpdatedAt(System.currentTimeMillis());
            userRepository.save(user);

            auditLogger.logSuccessfulLogin(user.getId(), email, ipAddress);
            logger.info("User logged in: {}", user.getId());
            return jwtUtil.generateToken(user.getId());
        } catch (Exception e) {
            logger.error("Login error: {}", e.getMessage());
            throw e;
        }
    }

    public void requestPasswordReset(String email, String ipAddress) {
        if (!InputValidator.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email");
        }

        String emailHash = hashEmail(email);
        User user = userRepository.findByEmailHash(emailHash);

        if (user == null) {
            auditLogger.logFailedRegistration(email, ipAddress, "Password reset request for non-existent user");
            return;
        }

        generatePasswordResetToken(user);
        logger.info("Password reset token generated for user: {}", user.getId());
    }

    public void resetPassword(String tokenHash, String newPassword, String ipAddress) {
        if (!InputValidator.isValidPassword(newPassword)) {
            throw new IllegalArgumentException("Password does not meet security requirements");
        }

        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(tokenHash);
        if (token == null || token.isExpired() || token.getUsed()) {
            auditLogger.logFailedLogin(null, ipAddress, "Invalid or expired password reset token");
            throw new RuntimeException("Invalid or expired password reset token");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(System.currentTimeMillis());
        userRepository.save(user);

        token.setUsed(true);
        passwordResetTokenRepository.save(token);
        auditLogger.logSuccessfulLogin(user.getId(), user.getEmail(), ipAddress);
        logger.info("Password reset successful for user: {}", user.getId());
    }

    public void verifyEmail(String tokenHash) {
        EmailVerificationToken token = emailVerificationTokenRepository.findByTokenHash(tokenHash);
        if (token == null || token.isExpired()) {
            throw new RuntimeException("Invalid or expired email verification token");
        }

        User user = token.getUser();
        user.setEmailVerified(true);
        user.setUpdatedAt(System.currentTimeMillis());
        userRepository.save(user);

        token.setVerified(true);
        emailVerificationTokenRepository.save(token);
        logger.info("Email verified for user: {}", user.getId());
    }

    private void generatePasswordResetToken(User user) {
        String token = generateSecureToken();
        String tokenHash = hashToken(token);
        long expiresAt = System.currentTimeMillis() + PASSWORD_RESET_TOKEN_EXPIRY;
        PasswordResetToken resetToken = new PasswordResetToken(
                "prt_" + UUID.randomUUID().toString().replace("-", ""),
                user,
                tokenHash,
                expiresAt);
        passwordResetTokenRepository.save(resetToken);
    }

    private void generateEmailVerificationToken(User user) {
        String token = generateSecureToken();
        String tokenHash = hashToken(token);
        long expiresAt = System.currentTimeMillis() + EMAIL_VERIFICATION_TOKEN_EXPIRY;
        EmailVerificationToken verifyToken = new EmailVerificationToken(
                "evt_" + UUID.randomUUID().toString().replace("-", ""),
                user,
                tokenHash,
                expiresAt);
        emailVerificationTokenRepository.save(verifyToken);
    }

    private String hashEmail(String email) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(email.trim().toLowerCase().getBytes());
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String encryptEmail(String email) {
        try {
            if (AES_ENCRYPTION_KEY == null || AES_ENCRYPTION_KEY.isEmpty()) {
                return Base64.getEncoder().encodeToString(email.getBytes());
            }

            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec key = new SecretKeySpec(AES_ENCRYPTION_KEY.getBytes(), 0, 16, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedData = cipher.doFinal(email.getBytes());
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            logger.warn("Email encryption failed, falling back to base64");
            return Base64.getEncoder().encodeToString(email.getBytes());
        }
    }

    private String hashToken(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(token.getBytes());
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
