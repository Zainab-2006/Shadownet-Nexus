package com.shadownet.nexus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvidenceDTO {
    private Long id;
    private String evidenceCode;
    private String title;
    private String summary;
    private Long sourceChapterId;
    private Long sourceSceneId;
    private Long sourceChoiceId;
    private String operatorInterpretation;
    private String missionRelevanceTag;
    private String discoveredAt;
    private Boolean newlyDiscovered;
}
