package com.shadownet.nexus.dto;

public class ChallengeListDTO {
    private String id;
    private String name;
    private String title;
    private String category;
    private String difficulty;
    private Integer points;
    private String description;
    private int solveCount;
    private int solves;
    private boolean solved;
    private boolean isSolved;
    private String author;
    private boolean hasDockerRuntime;

    public ChallengeListDTO() {
    }

    public ChallengeListDTO(String id, String name, String category, String difficulty, Integer points,
            String description, int solveCount, boolean solved, String author, boolean hasDockerRuntime) {
        this.id = id;
        this.name = name;
        this.title = name;
        this.category = category;
        this.difficulty = difficulty;
        this.points = points;
        this.description = description;
        this.solveCount = solveCount;
        this.solves = solveCount;
        this.solved = solved;
        this.isSolved = solved;
        this.author = author;
        this.hasDockerRuntime = hasDockerRuntime;
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

    public int getSolveCount() {
        return solveCount;
    }

    public void setSolveCount(int solveCount) {
        this.solveCount = solveCount;
        this.solves = solveCount;
    }

    public int getSolves() {
        return solves;
    }

    public void setSolves(int solves) {
        this.solves = solves;
        this.solveCount = solves;
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
        this.isSolved = solved;
    }

    public boolean getIsSolved() {
        return isSolved;
    }

    public void setIsSolved(boolean isSolved) {
        this.isSolved = isSolved;
        this.solved = isSolved;
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
}
