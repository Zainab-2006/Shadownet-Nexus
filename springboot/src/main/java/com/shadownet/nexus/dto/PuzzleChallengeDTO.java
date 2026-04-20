package com.shadownet.nexus.dto;

import java.util.List;

public class PuzzleChallengeDTO {
    private String id;
    private String name;
    private String title;
    private String category;
    private String difficulty;
    private Integer points;
    private String description;
    private String author;
    private boolean hasDockerRuntime;
    private List<PuzzleStageDTO> stages;

    public PuzzleChallengeDTO() {
    }

    public PuzzleChallengeDTO(String id, String name, String category, String difficulty, Integer points,
            String description, String author, boolean hasDockerRuntime, List<PuzzleStageDTO> stages) {
        this.id = id;
        this.name = name;
        this.title = name;
        this.category = category;
        this.difficulty = difficulty;
        this.points = points;
        this.description = description;
        this.author = author;
        this.hasDockerRuntime = hasDockerRuntime;
        this.stages = stages;
    }

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
        this.title = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isHasDockerRuntime() {
        return hasDockerRuntime;
    }

    public boolean getHasDockerRuntime() {
        return hasDockerRuntime;
    }

    public void setHasDockerRuntime(boolean hasDockerRuntime) {
        this.hasDockerRuntime = hasDockerRuntime;
    }

    public List<PuzzleStageDTO> getStages() {
        return stages;
    }

    public void setStages(List<PuzzleStageDTO> stages) {
        this.stages = stages;
    }
}
