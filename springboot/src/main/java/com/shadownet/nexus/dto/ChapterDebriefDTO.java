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
public class ChapterDebriefDTO {
    private Long chapterId;
    private String playerConclusion;
    private List<EvidenceDTO> evidenceFound;
    private List<String> evidenceMissed;
    private Integer trustOutcome;
    private String nextOperationalRisk;
}
