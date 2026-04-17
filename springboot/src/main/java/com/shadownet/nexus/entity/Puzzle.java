package com.shadownet.nexus.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "puzzles")
public class Puzzle {

    @Id
    private String id;

    @Column(nullable = false)
    private String missionId;

    private String puzzleType;

    private String difficulty;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private String solutionHash;

    private Long createdAt;

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMissionId() {
        return missionId;
    }

    public void setMissionId(String missionId) {
        this.missionId = missionId;
    }

    public String getPuzzleType() {
        return puzzleType;
    }

    public void setPuzzleType(String puzzleType) {
        this.puzzleType = puzzleType;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getSolutionHash() {
        return solutionHash;
    }

    public void setSolutionHash(String solutionHash) {
        this.solutionHash = solutionHash;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}