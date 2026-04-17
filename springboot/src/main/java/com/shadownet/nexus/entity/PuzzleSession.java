package com.shadownet.nexus.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;

@Entity
@Table(name = "puzzle_sessions")
public class PuzzleSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "challenge_id", nullable = false)
    private String challengeId;

    @Column(name = "current_stage", nullable = false)
    private int currentStage = 1;

    @Column(name = "hints_used", nullable = false)
    private int hintsUsed = 0;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false, columnDefinition = "TINYINT")
    private boolean completed = false;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    // Constructors
    public PuzzleSession() {
    }

    public PuzzleSession(String userId, String challengeId) {
        this.userId = userId;
        this.challengeId = challengeId;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
    }

    // Getters/Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public int getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(int currentStage) {
        this.currentStage = currentStage;
        this.updatedAt = System.currentTimeMillis();
    }

    public int getHintsUsed() {
        return hintsUsed;
    }

    public void setHintsUsed(int hintsUsed) {
        this.hintsUsed = hintsUsed;
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
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

    public void touch() {
        this.updatedAt = System.currentTimeMillis();
    }
}
