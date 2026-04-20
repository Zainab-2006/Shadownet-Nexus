package com.shadownet.nexus.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "challenges")
public class Challenge {

    private int maxSolves = 100;

    private String firstBloodUserId;

    private Long firstBloodAt;

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String difficulty;

    @Column(nullable = false)
    private Integer points;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private String flagHash;

    private Long createdAt;

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonIgnore
    public String getFlagHash() {
        return flagHash;
    }

    public void setFlagHash(String flagHash) {
        this.flagHash = flagHash;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    // Dynamic Scoring Fields
    public int getMaxSolves() {
        return maxSolves;
    }

    public void setMaxSolves(int maxSolves) {
        this.maxSolves = maxSolves;
    }

    public String getFirstBloodUserId() {
        return firstBloodUserId;
    }

    public void setFirstBloodUserId(String firstBloodUserId) {
        this.firstBloodUserId = firstBloodUserId;
    }

    public Long getFirstBloodAt() {
        return firstBloodAt;
    }

    public void setFirstBloodAt(Long firstBloodAt) {
        this.firstBloodAt = firstBloodAt;
    }

    // Pillar 3: Coaching Fields
    private String stages; // JSON: [{"briefing": "...", "flagHash": "...", "learningContent": "..."}]

    private String hints; // JSON: [{"content": "...", "personalized": false}]

    private String explanation; // Post-solve real-world lesson

    // Pillar 5: Container Challenges
    private String dockerImage;

    @Column(columnDefinition = "TEXT")
    @JsonIgnore
    public String getStages() {
        return stages;
    }

    public void setStages(String stages) {
        this.stages = stages;
    }

    @Column(columnDefinition = "TEXT")
    @JsonIgnore
    public String getHints() {
        return hints;
    }

    public void setHints(String hints) {
        this.hints = hints;
    }

    @Column(columnDefinition = "TEXT")
    @JsonIgnore
    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    @JsonIgnore
    public String getDockerImage() {
        return dockerImage;
    }

    public void setDockerImage(String dockerImage) {
        this.dockerImage = dockerImage;
    }
}
