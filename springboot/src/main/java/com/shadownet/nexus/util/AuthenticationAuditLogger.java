package com.shadownet.nexus.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Authentication audit logger
 * Tracks failed login attempts and suspicious activity
 */
@Component
public class AuthenticationAuditLogger {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // Track failed login attempts per IP
    private static final ConcurrentHashMap<String, FailedAttemptTracker> failedAttempts = new ConcurrentHashMap<>();

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 15 * 60 * 1000; // 15 minutes

    /**
     * Log successful login
     */
    public void logSuccessfulLogin(String userId, String email, String ipAddress) {
        auditLogger.info("SUCCESSFUL_LOGIN user_id={} email={} ip_address={}",
                userId, maskEmail(email), ipAddress);
        clearFailedAttempts(ipAddress);
    }

    /**
     * Log failed login
     */
    public void logFailedLogin(String email, String ipAddress, String reason) {
        FailedAttemptTracker tracker = failedAttempts.computeIfAbsent(ipAddress, k -> new FailedAttemptTracker());
        tracker.increment();

        auditLogger.warn("FAILED_LOGIN email={} ip_address={} reason={} attempts={}",
                maskEmail(email), ipAddress, reason, tracker.getAttempts());

        if (tracker.getAttempts() >= MAX_FAILED_ATTEMPTS && !tracker.isLocked()) {
            tracker.lock();
            securityLogger.error("BRUTE_FORCE_DETECTED ip_address={} attempts={} locked_until={}",
                    ipAddress, tracker.getAttempts(), tracker.getLockedUntil());
        }
    }

    /**
     * Log successful registration
     */
    public void logSuccessfulRegistration(String userId, String email, String ipAddress) {
        auditLogger.info("SUCCESSFUL_REGISTRATION user_id={} email={} ip_address={}",
                userId, maskEmail(email), ipAddress);
    }

    /**
     * Log failed registration
     */
    public void logFailedRegistration(String email, String ipAddress, String reason) {
        auditLogger.warn("FAILED_REGISTRATION email={} ip_address={} reason={}",
                maskEmail(email), ipAddress, reason);
    }

    /**
     * Log unauthorized access attempt
     */
    public void logUnauthorizedAccess(String userId, String endpoint, String ipAddress, String reason) {
        securityLogger.warn("UNAUTHORIZED_ACCESS user_id={} endpoint={} ip_address={} reason={}",
                userId, endpoint, ipAddress, reason);
    }

    /**
     * Log suspicious input detected
     */
    public void logSuspiciousInput(String userId, String endpoint, String inputType, String ipAddress) {
        securityLogger.error("SUSPICIOUS_INPUT_DETECTED user_id={} endpoint={} type={} ip_address={}",
                userId, endpoint, inputType, ipAddress);
    }

    /**
     * Log data access
     */
    public void logDataAccess(String userId, String resourceType, String resourceId, String action, String ipAddress) {
        auditLogger.info("DATA_ACCESS user_id={} resource_type={} resource_id={} action={} ip_address={}",
                userId, resourceType, resourceId, action, ipAddress);
    }

    /**
     * Check if IP is locked due to brute force
     */
    public boolean isIpLocked(String ipAddress) {
        FailedAttemptTracker tracker = failedAttempts.get(ipAddress);
        if (tracker == null) {
            return false;
        }
        if (tracker.isLocked() && tracker.isLockExpired()) {
            clearFailedAttempts(ipAddress);
            return false;
        }
        return tracker.isLocked();
    }

    /**
     * Clear failed attempts for an IP
     */
    private void clearFailedAttempts(String ipAddress) {
        failedAttempts.remove(ipAddress);
    }

    /**
     * Mask email for logging (show only first char + domain)
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        return parts[0].charAt(0) + "***@" + parts[1];
    }

    /**
     * Internal class to track failed attempts per IP
     */
    private static class FailedAttemptTracker {
        private final AtomicInteger attempts = new AtomicInteger(0);
        private volatile boolean locked = false;
        private volatile long lockedUntil = 0;

        void increment() {
            attempts.incrementAndGet();
        }

        int getAttempts() {
            return attempts.get();
        }

        void lock() {
            this.locked = true;
            this.lockedUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS;
        }

        boolean isLocked() {
            return locked;
        }

        boolean isLockExpired() {
            return System.currentTimeMillis() > lockedUntil;
        }

        String getLockedUntil() {
            return LocalDateTime.now().plusSeconds((lockedUntil - System.currentTimeMillis()) / 1000).format(formatter);
        }
    }
}
