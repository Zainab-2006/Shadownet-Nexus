package com.shadownet.nexus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperatorInterpretationDTO {
    private String operatorId;
    private String lens;
    private String evidenceAngle;
    private String missionEmphasis;
}
