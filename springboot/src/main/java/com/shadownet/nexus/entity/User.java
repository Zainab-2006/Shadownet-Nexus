package com.shadownet.nexus.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "users")
public class User {

    @Id
    private String id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String emailHash;

    @Column(columnDefinition = "TEXT")
    private String emailEncrypted;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer score = 0;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer xp = 0;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 1")
    private Integer level = 1;

    private String selectedOperator;

    @Column(columnDefinition = "TEXT")
    private String storyProgress;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer failedLoginAttempts = 0;

    private Long lastFailedLoginAt;

    private Long lockedUntil;

    private Long createdAt;

    private Long updatedAt;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Boolean emailVerified = false;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Boolean accountLocked = false;

    private Long lastLoginAt;

    private String emailVerifyTokenHash;

    private Long emailVerifyExpires;

    private String passwordResetTokenHash;

    private Long passwordResetExpires;

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailHash() {
        return emailHash;
    }

    public void setEmailHash(String emailHash) {
        this.emailHash = emailHash;
    }

    public String getEmailEncrypted() {
        return emailEncrypted;
    }

    public void setEmailEncrypted(String emailEncrypted) {
        this.emailEncrypted = emailEncrypted;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getXp() {
        return xp;
    }

    public void setXp(Integer xp) {
        this.xp = xp;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getSelectedOperator() {
        return selectedOperator;
    }

    public void setSelectedOperator(String selectedOperator) {
        this.selectedOperator = selectedOperator;
    }

    public String getStoryProgress() {
        return storyProgress;
    }

    public void setStoryProgress(String storyProgress) {
        this.storyProgress = storyProgress;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public Long getLastFailedLoginAt() {
        return lastFailedLoginAt;
    }

    public void setLastFailedLoginAt(Long lastFailedLoginAt) {
        this.lastFailedLoginAt = lastFailedLoginAt;
    }

    public Long getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(Long lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Boolean getAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(Boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public Long getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Long lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public String getEmailVerifyTokenHash() {
        return emailVerifyTokenHash;
    }

    public void setEmailVerifyTokenHash(String emailVerifyTokenHash) {
        this.emailVerifyTokenHash = emailVerifyTokenHash;
    }

    public Long getEmailVerifyExpires() {
        return emailVerifyExpires;
    }

    public void setEmailVerifyExpires(Long emailVerifyExpires) {
        this.emailVerifyExpires = emailVerifyExpires;
    }

    public String getPasswordResetTokenHash() {
        return passwordResetTokenHash;
    }

    public void setPasswordResetTokenHash(String passwordResetTokenHash) {
        this.passwordResetTokenHash = passwordResetTokenHash;
    }

    public Long getPasswordResetExpires() {
        return passwordResetExpires;
    }

    public void setPasswordResetExpires(Long passwordResetExpires) {
        this.passwordResetExpires = passwordResetExpires;
    }
}