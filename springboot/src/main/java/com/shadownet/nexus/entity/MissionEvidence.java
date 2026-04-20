package com.shadownet.nexus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mission_evidence")
public class MissionEvidence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mission_instance_id", nullable = false)
    private Long missionInstanceId;

    @Column(name = "evidence_key", nullable = false, length = 100)
    private String evidenceKey;

    @Column(name = "evidence_type", nullable = false, length = 60)
    private String evidenceType;

    @Column(nullable = false)
    private Boolean found = false;

    @Column(name = "content_json", columnDefinition = "LONGTEXT")
    private String contentJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public Long getMissionInstanceId() { return missionInstanceId; }
    public void setMissionInstanceId(Long missionInstanceId) { this.missionInstanceId = missionInstanceId; }
    public String getEvidenceKey() { return evidenceKey; }
    public void setEvidenceKey(String evidenceKey) { this.evidenceKey = evidenceKey; }
    public String getEvidenceType() { return evidenceType; }
    public void setEvidenceType(String evidenceType) { this.evidenceType = evidenceType; }
    public Boolean getFound() { return found; }
    public void setFound(Boolean found) { this.found = found; }
    public String getContentJson() { return contentJson; }
    public void setContentJson(String contentJson) { this.contentJson = contentJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
