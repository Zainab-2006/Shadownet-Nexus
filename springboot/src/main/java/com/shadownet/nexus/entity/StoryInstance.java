package com.shadownet.nexus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "story_instances")
public class StoryInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instance_key", nullable = false, unique = true, length = 120)
    private String instanceKey;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "operator_code", nullable = false, length = 80)
    private String operatorCode;

    @Column(name = "chapter_code", nullable = false, length = 80)
    private String chapterCode;

    @Column(name = "scene_code", nullable = false, length = 80)
    private String sceneCode;

    @Column(name = "trust_level", nullable = false)
    private Integer trustLevel = 0;

    @Column(name = "affinity_level", nullable = false)
    private Integer affinityLevel = 0;

    @Column(nullable = false, length = 30)
    private String status = "ACTIVE";

    @Column(name = "choices_count", nullable = false)
    private Integer choicesCount = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public Long getId() { return id; }
    public String getInstanceKey() { return instanceKey; }
    public void setInstanceKey(String instanceKey) { this.instanceKey = instanceKey; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getOperatorCode() { return operatorCode; }
    public void setOperatorCode(String operatorCode) { this.operatorCode = operatorCode; }
    public String getChapterCode() { return chapterCode; }
    public void setChapterCode(String chapterCode) { this.chapterCode = chapterCode; }
    public String getSceneCode() { return sceneCode; }
    public void setSceneCode(String sceneCode) { this.sceneCode = sceneCode; }
    public Integer getTrustLevel() { return trustLevel; }
    public void setTrustLevel(Integer trustLevel) { this.trustLevel = trustLevel; }
    public Integer getAffinityLevel() { return affinityLevel; }
    public void setAffinityLevel(Integer affinityLevel) { this.affinityLevel = affinityLevel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getChoicesCount() { return choicesCount; }
    public void setChoicesCount(Integer choicesCount) { this.choicesCount = choicesCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
