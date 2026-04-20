package com.shadownet.nexus.dto;

public class StoryStartRequest {
    private String operatorCode;
    private String chapterCode;
    private String sceneCode;

    public String getOperatorCode() { return operatorCode; }
    public void setOperatorCode(String operatorCode) { this.operatorCode = operatorCode; }
    public String getChapterCode() { return chapterCode; }
    public void setChapterCode(String chapterCode) { this.chapterCode = chapterCode; }
    public String getSceneCode() { return sceneCode; }
    public void setSceneCode(String sceneCode) { this.sceneCode = sceneCode; }
}
