package com.shadownet.nexus.dto;

public class PCGChallengeSubmitResponse {
    private boolean correct;
    private String message;
    private Integer pointsAwarded;
    private String status;

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Integer getPointsAwarded() { return pointsAwarded; }
    public void setPointsAwarded(Integer pointsAwarded) { this.pointsAwarded = pointsAwarded; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
