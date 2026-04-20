package com.shadownet.nexus.dto;

public class MissionStartRequest {
    private String missionCode;
    private String squadId;

    public String getMissionCode() { return missionCode; }
    public void setMissionCode(String missionCode) { this.missionCode = missionCode; }
    public String getSquadId() { return squadId; }
    public void setSquadId(String squadId) { this.squadId = squadId; }
}
