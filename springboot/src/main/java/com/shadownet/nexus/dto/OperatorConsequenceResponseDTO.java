package com.shadownet.nexus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperatorConsequenceResponseDTO {
    private String operatorId;
    private String missionId;
    private String choiceId;
    private String outcome;
    private Integer trustDelta;
    private Integer updatedTrust;
    private String targetEntity;
    private List<MissionConsequenceDTO> missionChanges;
    private List<String> consequenceFlags;
    private StoryConsequenceSummaryDTO consequenceSummary;
}
