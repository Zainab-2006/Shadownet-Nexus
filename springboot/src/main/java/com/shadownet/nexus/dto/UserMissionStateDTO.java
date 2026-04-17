package com.shadownet.nexus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMissionStateDTO {
    private String missionId;
    private String state;
    private String reason;
    private Long sourceChapterId;
    private Long sourceSceneId;
    private Long sourceChoiceId;
    private String updatedAt;
}
