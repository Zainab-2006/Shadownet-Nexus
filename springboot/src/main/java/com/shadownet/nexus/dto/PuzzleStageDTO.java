package com.shadownet.nexus.dto;

public class PuzzleStageDTO {
    private String briefing;
    private String objective;
    private String evidence;
    private String submitFormat;
    private String learningContent;

    public PuzzleStageDTO() {
    }

    public PuzzleStageDTO(String briefing, String objective, String evidence, String submitFormat,
            String learningContent) {
        this.briefing = briefing;
        this.objective = objective;
        this.evidence = evidence;
        this.submitFormat = submitFormat;
        this.learningContent = learningContent;
    }

    public String getBriefing() {
        return briefing;
    }

    public void setBriefing(String briefing) {
        this.briefing = briefing;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    public String getSubmitFormat() {
        return submitFormat;
    }

    public void setSubmitFormat(String submitFormat) {
        this.submitFormat = submitFormat;
    }

    public String getLearningContent() {
        return learningContent;
    }

    public void setLearningContent(String learningContent) {
        this.learningContent = learningContent;
    }
}
