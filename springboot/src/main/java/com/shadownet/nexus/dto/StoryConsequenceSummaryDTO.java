package com.shadownet.nexus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryConsequenceSummaryDTO {
    private String summary;
    private String playerConclusion;
    private String nextOperationalRisk;
}
