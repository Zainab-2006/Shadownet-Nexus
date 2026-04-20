package com.shadownet.nexus.dto;

import java.util.List;

public class MissionViewDTO {
    private String instanceKey;
    private String missionCode;
    private String status;
    private String phase;
    private Integer trustScore;
    private Integer suspicionScore;
    private Integer credits;
    private Integer decisionsCount;
    private List<MissionEvidenceViewDTO> visibleEvidence;

    public String getInstanceKey() { return instanceKey; }
    public void setInstanceKey(String instanceKey) { this.instanceKey = instanceKey; }
    public String getMissionCode() { return missionCode; }
    public void setMissionCode(String missionCode) { this.missionCode = missionCode; }
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
    public Integer getDecisionsCount() { return decisionsCount; }
    public void setDecisionsCount(Integer decisionsCount) { this.decisionsCount = decisionsCount; }
    public List<MissionEvidenceViewDTO> getVisibleEvidence() { return visibleEvidence; }
    public void setVisibleEvidence(List<MissionEvidenceViewDTO> visibleEvidence) { this.visibleEvidence = visibleEvidence; }
}
