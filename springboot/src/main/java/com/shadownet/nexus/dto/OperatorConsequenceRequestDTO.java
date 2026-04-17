package com.shadownet.nexus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperatorConsequenceRequestDTO {
    private String missionId;
    private String choiceId;
    private String outcome;
    private String action;
}
