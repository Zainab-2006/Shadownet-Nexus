package com.shadownet.nexus.dto;

public class MissionDecisionResponse {
    private boolean success;
    private String message;
    private Integer trustScore;
    private Integer suspicionScore;
    private Integer credits;
    private String phase;
    private String status;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Integer getTrustScore() { return trustScore; }
    public void setTrustScore(Integer trustScore) { this.trustScore = trustScore; }
    public Integer getSuspicionScore() { return suspicionScore; }
    public void setSuspicionScore(Integer suspicionScore) { this.suspicionScore = suspicionScore; }
    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
