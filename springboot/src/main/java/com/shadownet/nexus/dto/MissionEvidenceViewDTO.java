package com.shadownet.nexus.dto;

public class MissionEvidenceViewDTO {
    private String evidenceKey;
    private String evidenceType;
    private boolean found;
    private String contentJson;

    public String getEvidenceKey() { return evidenceKey; }
    public void setEvidenceKey(String evidenceKey) { this.evidenceKey = evidenceKey; }
    public String getEvidenceType() { return evidenceType; }
    public void setEvidenceType(String evidenceType) { this.evidenceType = evidenceType; }
    public boolean isFound() { return found; }
    public void setFound(boolean found) { this.found = found; }
    public String getContentJson() { return contentJson; }
    public void setContentJson(String contentJson) { this.contentJson = contentJson; }
}
