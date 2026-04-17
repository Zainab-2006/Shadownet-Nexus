package com.shadownet.nexus.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "user_events", indexes = {
        @Index(name = "idx_user_events_user_time", columnList = "user_id,created_at DESC"),
        @Index(name = "idx_user_events_type", columnList = "event_type"),
        @Index(name = "idx_user_events_user_category", columnList = "user_id,category")
})
public class UserEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID DEFAULT gen_random_uuid()")
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false, length = 50)
    private String eventType; // challenge_view, flag_submit_correct, flag_submit_wrong, hint_view, etc.

    @Column(length = 255)
    private String challengeId;

    @Column(length = 50)
    private String category;

    @Column(name = "metadata", columnDefinition = "JSONB DEFAULT '{}'")
    private String metadataJson;

    @Column(name = "fail_count", nullable = false)
    private int failCount = 0;

    @Column(name = "session_duration_seconds", nullable = false)
    private int sessionDurationSeconds = 0;

    @Column(name = "created_at", nullable = false)
    private Long createdAt = System.currentTimeMillis();

    // Constructors
    public UserEvent() {
    }

    public UserEvent(String userId, String eventType, String challengeId, String category,
            Map<String, Object> metadata) {
        this.userId = userId;
        this.eventType = eventType;
        this.challengeId = challengeId;
        this.category = category;
        try {
            this.metadataJson = new ObjectMapper().writeValueAsString(metadata != null ? metadata : Map.of());
        } catch (Exception e) {
            this.metadataJson = "{}";
        }
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

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    public Map<String, Object> getMetadata() {
        try {
            return new ObjectMapper().readValue(metadataJson, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }

    public void setMetadata(Map<String, Object> metadata) {
        try {
            this.metadataJson = new ObjectMapper().writeValueAsString(metadata);
        } catch (Exception e) {
            this.metadataJson = "{}";
        }
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public int getSessionDurationSeconds() {
        return sessionDurationSeconds;
    }

    public void setSessionDurationSeconds(int sessionDurationSeconds) {
        this.sessionDurationSeconds = sessionDurationSeconds;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}
