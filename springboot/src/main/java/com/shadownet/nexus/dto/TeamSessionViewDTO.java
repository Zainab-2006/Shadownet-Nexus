package com.shadownet.nexus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamSessionViewDTO {
    private String id;
    private String teamId;
    private String sessionId;
    private String missionId;
    private String phase;
    private int evidenceCount;
    private List<MemberViewDTO> members;
    private List<Map<String, Object>> activity;
    private Map<String, Object> trust;
    private Map<String, Object> evidence;
    private Map<String, Object> accusation;
    private boolean accusationUnlocked;
    private Object accusationResult;
}
