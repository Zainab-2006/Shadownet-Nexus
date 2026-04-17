package com.shadownet.nexus.dto;

public class TrustResponse {
    private int trustScore;
    private String message;

    public TrustResponse(int trustScore, String message) {
        this.trustScore = trustScore;
        this.message = message;
    }

    // Getters and setters
    public int getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(int trustScore) {
        this.trustScore = trustScore;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
