package com.shadownet.nexus.dto;

import lombok.Data;

@Data
public class ChallengeSubmitRequest {
    private String challengeId;
    private String flag;
}
