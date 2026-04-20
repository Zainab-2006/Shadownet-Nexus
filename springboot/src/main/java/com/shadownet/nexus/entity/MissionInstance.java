package com.shadownet.nexus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mission_instances")
public class MissionInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instance_key", nullable = false, unique = true, length = 120)
    private String instanceKey;

    @Column(name = "mission_code", nullable = false, length = 80)
    private String missionCode;

    @Column(name = "owner_user_id", nullable = false, length = 64)
    private String ownerUserId;

    @Column(name = "squad_id", length = 64)
    private String squadId;

    @Column(nullable = false, length = 30)
    private String status = "ACTIVE";

    @Column(nullable = false, length = 50)
    private String phase = "BRIEFING";

    @Column(name = "trust_score", nullable = false)
    private Integer trustScore = 0;

    @Column(name = "suspicion_score", nullable = false)
    private Integer suspicionScore = 0;

    @Column(nullable = false)
    private Integer credits = 0;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    @Column(name = "decisions_count", nullable = false)
    private Integer decisionsCount = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public Long getId() { return id; }
    public String getInstanceKey() { return instanceKey; }
    public void setInstanceKey(String instanceKey) { this.instanceKey = instanceKey; }
    public String getMissionCode() { return missionCode; }
    public void setMissionCode(String missionCode) { this.missionCode = missionCode; }
    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(String ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getSquadId() { return squadId; }
    public void setSquadId(String squadId) { this.squadId = squadId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }
    public Integer getTrustScore() { return trustScore; }
    public void setTrustScore(Integer trustScore) { this.trustScore = trustScore; }
    public Integer getSuspicionScore() { return suspicionScore; }
    public void setSuspicionScore(Integer suspicionScore) { this.suspicionScore = suspicionScore; }
    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
    public Integer getAttemptCount() { return attemptCount; }
    public void setAttemptCount(Integer attemptCount) { this.attemptCount = attemptCount; }
    public Integer getDecisionsCount() { return decisionsCount; }
    public void setDecisionsCount(Integer decisionsCount) { this.decisionsCount = decisionsCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
