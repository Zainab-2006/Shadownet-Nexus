package com.shadownet.nexus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberViewDTO {
    private String userId;
    private String username;
    private String displayName;
    private String operatorId;
    private String operatorCodename;
    private String operatorPortrait;
    private String role;
    private boolean ready;
    private boolean connected;
    private String contributionSummary;
}
