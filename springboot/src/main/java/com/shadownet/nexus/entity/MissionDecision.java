package com.shadownet.nexus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mission_decisions")
public class MissionDecision {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mission_instance_id", nullable = false)
    private Long missionInstanceId;

    @Column(name = "decision_key", nullable = false, length = 100)
    private String decisionKey;

    @Column(name = "chosen_option", nullable = false, length = 100)
    private String chosenOption;

    @Column(name = "trust_delta", nullable = false)
    private Integer trustDelta = 0;

    @Column(name = "suspicion_delta", nullable = false)
    private Integer suspicionDelta = 0;

    @Column(name = "credits_delta", nullable = false)
    private Integer creditsDelta = 0;

    @Column(name = "consequence_json", columnDefinition = "LONGTEXT")
    private String consequenceJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public Long getMissionInstanceId() { return missionInstanceId; }
    public void setMissionInstanceId(Long missionInstanceId) { this.missionInstanceId = missionInstanceId; }
    public String getDecisionKey() { return decisionKey; }
    public void setDecisionKey(String decisionKey) { this.decisionKey = decisionKey; }
    public String getChosenOption() { return chosenOption; }
    public void setChosenOption(String chosenOption) { this.chosenOption = chosenOption; }
    public Integer getTrustDelta() { return trustDelta; }
    public void setTrustDelta(Integer trustDelta) { this.trustDelta = trustDelta; }
    public Integer getSuspicionDelta() { return suspicionDelta; }
    public void setSuspicionDelta(Integer suspicionDelta) { this.suspicionDelta = suspicionDelta; }
    public Integer getCreditsDelta() { return creditsDelta; }
    public void setCreditsDelta(Integer creditsDelta) { this.creditsDelta = creditsDelta; }
    public String getConsequenceJson() { return consequenceJson; }
    public void setConsequenceJson(String consequenceJson) { this.consequenceJson = consequenceJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
