package com.shadownet.nexus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionResponseDTO {
    
    private StoryProgressDTO progress;
    
    private Long nextSceneId;
    
    private Long nextChapterId;
    
    private Integer trustImpact;

    private Integer trustDelta;

    private Integer updatedTrust;
    
    private String targetEntity;

    private java.util.List<EvidenceDTO> evidenceGained;

    private java.util.List<String> consequenceFlags;

    private java.util.List<String> unlockedMissionIds;

    private java.util.List<String> recommendedMissionIds;

    private java.util.List<MissionConsequenceDTO> missionChanges;

    private StoryConsequenceSummaryDTO consequenceSummary;

    private OperatorInterpretationDTO operatorInterpretation;
}

