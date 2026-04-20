package com.shadownet.nexus.dto;

public class MissionEvidenceClaimRequest {
    private String instanceKey;
    private String evidenceKey;

    public String getInstanceKey() { return instanceKey; }
    public void setInstanceKey(String instanceKey) { this.instanceKey = instanceKey; }
    public String getEvidenceKey() { return evidenceKey; }
    public void setEvidenceKey(String evidenceKey) { this.evidenceKey = evidenceKey; }
}
