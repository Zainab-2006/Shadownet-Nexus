package com.shadownet.nexus.dto;

import java.util.List;

public class StoryViewDTO {
    private String instanceKey;
    private String operatorCode;
    private String chapterCode;
    private String sceneCode;
    private String title;
    private String sceneText;
    private Integer trustLevel;
    private Integer affinityLevel;
    private Integer choicesCount;
    private String status;
    private List<StoryChoiceOptionDTO> choices;

    public String getInstanceKey() { return instanceKey; }
    public void setInstanceKey(String instanceKey) { this.instanceKey = instanceKey; }
    public String getOperatorCode() { return operatorCode; }
    public void setOperatorCode(String operatorCode) { this.operatorCode = operatorCode; }
    public String getChapterCode() { return chapterCode; }
    public void setChapterCode(String chapterCode) { this.chapterCode = chapterCode; }
    public String getSceneCode() { return sceneCode; }
    public void setSceneCode(String sceneCode) { this.sceneCode = sceneCode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSceneText() { return sceneText; }
    public void setSceneText(String sceneText) { this.sceneText = sceneText; }
    public Integer getTrustLevel() { return trustLevel; }
    public void setTrustLevel(Integer trustLevel) { this.trustLevel = trustLevel; }
    public Integer getAffinityLevel() { return affinityLevel; }
    public void setAffinityLevel(Integer affinityLevel) { this.affinityLevel = affinityLevel; }
    public Integer getChoicesCount() { return choicesCount; }
    public void setChoicesCount(Integer choicesCount) { this.choicesCount = choicesCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<StoryChoiceOptionDTO> getChoices() { return choices; }
    public void setChoices(List<StoryChoiceOptionDTO> choices) { this.choices = choices; }
}
