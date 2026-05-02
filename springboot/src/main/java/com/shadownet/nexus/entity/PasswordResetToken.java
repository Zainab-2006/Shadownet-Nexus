package com.shadownet.nexus.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Password reset token entity
 * Tokens expire after 1 hour
 */
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String tokenHash; // Hashed token (never store plain token)

    @Column(nullable = false)
    private Long expiresAt; // Timestamp when token expires

    @Column(nullable = false)
    private Long createdAt;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false, columnDefinition = "TINYINT")
    private Boolean used = false; // Token can only be used once

    public PasswordResetToken() {
    }

    public PasswordResetToken(String id, User user, String tokenHash, Long expiresAt) {
        this.id = id;
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getUsed() {
        return used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
}
