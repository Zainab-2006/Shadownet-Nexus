package com.shadownet.nexus.dto;

public class StoryDecisionRequest {
    private String instanceKey;
    private String choiceKey;
    private String chosenOption;

    public String getInstanceKey() { return instanceKey; }
    public void setInstanceKey(String instanceKey) { this.instanceKey = instanceKey; }
    public String getChoiceKey() { return choiceKey; }
    public void setChoiceKey(String choiceKey) { this.choiceKey = choiceKey; }
    public String getChosenOption() { return chosenOption; }
    public void setChosenOption(String chosenOption) { this.chosenOption = chosenOption; }
}
