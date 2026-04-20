package com.shadownet.nexus.dto;

public class PCGChallengeViewDTO {
    private String instanceKey;
    private String title;
    private String description;
    private String category;
    private String difficulty;
    private Integer points;
    private String artifactJson;
    private Integer attemptCount;
    private Integer hintsUsed;
    private String status;

    public String getInstanceKey() { return instanceKey; }
    public void setInstanceKey(String instanceKey) { this.instanceKey = instanceKey; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public String getArtifactJson() { return artifactJson; }
    public void setArtifactJson(String artifactJson) { this.artifactJson = artifactJson; }
    public Integer getAttemptCount() { return attemptCount; }
    public void setAttemptCount(Integer attemptCount) { this.attemptCount = attemptCount; }
    public Integer getHintsUsed() { return hintsUsed; }
    public void setHintsUsed(Integer hintsUsed) { this.hintsUsed = hintsUsed; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
