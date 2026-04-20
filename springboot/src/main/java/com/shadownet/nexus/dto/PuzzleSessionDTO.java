package com.shadownet.nexus.dto;

public class PuzzleSessionDTO {
    private String id;
    private int currentStage;
    private int hintsUsed;
    private boolean completed;
    private PuzzleChallengeDTO challenge;

    public PuzzleSessionDTO() {
    }

    public PuzzleSessionDTO(String id, int currentStage, int hintsUsed, boolean completed,
            PuzzleChallengeDTO challenge) {
        this.id = id;
        this.currentStage = currentStage;
        this.hintsUsed = hintsUsed;
        this.completed = completed;
        this.challenge = challenge;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(int currentStage) {
        this.currentStage = currentStage;
    }

    public int getHintsUsed() {
        return hintsUsed;
    }

    public void setHintsUsed(int hintsUsed) {
        this.hintsUsed = hintsUsed;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public PuzzleChallengeDTO getChallenge() {
        return challenge;
    }

    public void setChallenge(PuzzleChallengeDTO challenge) {
        this.challenge = challenge;
    }
}
