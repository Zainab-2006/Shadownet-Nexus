package com.shadownet.nexus.dto;

public class MissionDecisionRequest {
    private String instanceKey;
    private String decisionKey;
    private String chosenOption;

    public String getInstanceKey() { return instanceKey; }
    public void setInstanceKey(String instanceKey) { this.instanceKey = instanceKey; }
    public String getDecisionKey() { return decisionKey; }
    public void setDecisionKey(String decisionKey) { this.decisionKey = decisionKey; }
    public String getChosenOption() { return chosenOption; }
    public void setChosenOption(String chosenOption) { this.chosenOption = chosenOption; }
}
