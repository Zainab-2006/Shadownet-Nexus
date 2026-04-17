package com.shadownet.nexus.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.*;

@Entity
@Table(name = "team_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamSession {
    @Id
    private String id;

    @Column(name = "team_id", nullable = false)
    private String teamId;

    @ElementCollection
    @CollectionTable(name = "team_members", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "member_id")
    private List<String> members = new ArrayList<>();

    private String status = "waiting";

    private String traitorId;

    private String accusationResult;

    @Column(name = "mission_id")
    private String missionId;

    @Column(name = "leader_id")
    private String leaderId;

    @Column(name = "evidence_json", columnDefinition = "TEXT")
    private String evidenceJson = "{}";

    @Column(name = "ready_json", columnDefinition = "TEXT")
    private String readyJson = "{}";

    @Column(name = "activity_json", columnDefinition = "TEXT")
    private String activityJson = "[]";

    @Column(name = "created_at", nullable = false)
    private Long createdAt = System.currentTimeMillis();

    @Column(name = "time_started")
    private Long timeStarted;

    @Transient
    private Map<String, Integer> evidenceMap = new HashMap<>();

    @Transient
    private Map<String, Boolean> readyMap = new HashMap<>();

    @Transient
    private List<Map<String, Object>> activityLog = new ArrayList<>();

    @PostLoad
    public void parseEvidenceMap() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.evidenceMap = mapper.readValue(evidenceJson, new TypeReference<Map<String, Integer>>(){});
            this.readyMap = readyJson == null ? new HashMap<>() : mapper.readValue(readyJson, new TypeReference<Map<String, Boolean>>(){});
            this.activityLog = activityJson == null ? new ArrayList<>() : mapper.readValue(activityJson, new TypeReference<List<Map<String, Object>>>(){});
        } catch (Exception e) {
            this.evidenceMap = new HashMap<>();
            this.readyMap = new HashMap<>();
            this.activityLog = new ArrayList<>();
        }
    }

    @PrePersist
    @PreUpdate
    public void serializeEvidenceMap() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.evidenceJson = mapper.writeValueAsString(evidenceMap);
            this.readyJson = mapper.writeValueAsString(readyMap);
            this.activityJson = mapper.writeValueAsString(activityLog);
            this.updatedAt = System.currentTimeMillis();
        } catch (Exception e) {
            this.evidenceJson = "{}";
            this.readyJson = "{}";
            this.activityJson = "[]";
        }
    }

    @Column(name = "updated_at")
    private Long updatedAt;

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setAccusationResult(String result) {
        this.accusationResult = result;
    }

    public String getTraitorId() {
        return traitorId;
    }

    public void setTraitorId(String traitorId) {
        this.traitorId = traitorId;
    }
}
