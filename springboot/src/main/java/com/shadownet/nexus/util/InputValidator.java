package com.shadownet.nexus.util;

import org.apache.commons.lang3.StringUtils;
import java.util.regex.Pattern;

/**
 * Input validation and sanitization utility
 * Prevents SQL injection, XSS, and other input-based attacks
 */
public class InputValidator {

    // Email regex pattern (RFC 5322 simplified)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$");

    // Username pattern: alphanumeric, underscore, hyphen (3-32 chars)
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[A-Za-z0-9_-]{3,32}$");

    // Detect actual SQLi payloads instead of flagging normal punctuation.
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(?i)(\\b(select|insert|update|delete|drop|union|alter|create|truncate|exec|execute)\\b|(--|#|/\\*)|\\b(or|and)\\b\\s+[\"'0-9a-z_]+\\s*=\\s*[\"'0-9a-z_]+|\\bunion\\b\\s+\\bselect\\b|\\binformation_schema\\b|\\bsleep\\s*\\(|\\bbenchmark\\s*\\()",
            Pattern.CASE_INSENSITIVE);

    // XSS prevention - dangerous HTML/JS characters
    private static final Pattern XSS_PATTERN = Pattern.compile(
            "(<script|<iframe|javascript:|onerror=|onclick=|onload=|<img |<svg|<body|<frame|<frameset|<object|<applet|<embed|<link|<style|<meta|<base|<form)");

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        if (StringUtils.isBlank(email) || email.length() > 254) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate username format
     */
    public static boolean isValidUsername(String username) {
        if (StringUtils.isBlank(username)) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Validate password strength
     * Requirements:
     * - Minimum 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     * - At least one special character
     */
    public static boolean isValidPassword(String password) {
        if (StringUtils.isBlank(password) || password.length() < 8 || password.length() > 128) {
            return false;
        }

        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?].*");

        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
    }

    /**
     * Check for SQL injection attempts
     */
    public static boolean containsSqlInjectionAttempt(String input) {
        if (StringUtils.isBlank(input)) {
            return false;
        }
        return SQL_INJECTION_PATTERN.matcher(input).find();
    }

    /**
     * Check for XSS attempts
     */
    public static boolean containsXssAttempt(String input) {
        if (StringUtils.isBlank(input)) {
            return false;
        }
        return XSS_PATTERN.matcher(input.toLowerCase()).find();
    }

    /**
     * Sanitize input by removing dangerous characters
     */
    public static String sanitizeInput(String input) {
        if (StringUtils.isBlank(input)) {
            return input;
        }

        // Remove null bytes
        input = input.replaceAll("\0", "");

        // Remove script tags and dangerous patterns
        input = input.replaceAll("(?i)<script[^>]*>.*?</script>", "");
        input = input.replaceAll("(?i)<iframe[^>]*>.*?</iframe>", "");
        input = input.replaceAll("(?i)javascript:", "");

        // Remove event handlers
        input = input.replaceAll("(?i)(onerror|onclick|onload|onmouseover|onchange|onsubmit)=", "");

        return input.trim();
    }

    /**
     * Validate challenge/mission ID format (alphanumeric with hyphens)
     */
    public static boolean isValidChallengeId(String id) {
        if (StringUtils.isBlank(id) || id.length() > 50) {
            return false;
        }
        return id.matches("^[a-zA-Z0-9_-]+$");
    }

    /**
     * Validate UUID format
     */
    public static boolean isValidUuid(String uuid) {
        if (StringUtils.isBlank(uuid)) {
            return false;
        }
        return uuid.matches("^[a-f0-9]{8}-?[a-f0-9]{4}-?[a-f0-9]{4}-?[a-f0-9]{4}-?[a-f0-9]{12}$|^user_[a-f0-9]+$");
    }

    /**
     * Validate flag format (prevent injection)
     */
    public static boolean isValidFlag(String flag) {
        if (StringUtils.isBlank(flag) || flag.length() > 256) {
            return false;
        }
        // Allow alphanumeric, special chars, but no null bytes or newlines
        return !flag.contains("\0") && !flag.contains("\n") && !flag.contains("\r");
    }
}
