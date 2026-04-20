package com.shadownet.nexus.dto;

public class StoryDecisionResponse {
    private boolean success;
    private String message;
    private String nextSceneCode;
    private Integer trustLevel;
    private Integer affinityLevel;
    private String status;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getNextSceneCode() { return nextSceneCode; }
    public void setNextSceneCode(String nextSceneCode) { this.nextSceneCode = nextSceneCode; }
    public Integer getTrustLevel() { return trustLevel; }
    public void setTrustLevel(Integer trustLevel) { this.trustLevel = trustLevel; }
    public Integer getAffinityLevel() { return affinityLevel; }
    public void setAffinityLevel(Integer affinityLevel) { this.affinityLevel = affinityLevel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
