package com.shadownet.nexus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChallengeSubmitResponse {
    private boolean correct;
    private int pointsEarned;
    private String message;
    
    public boolean getCorrect() {
        return correct;
    }
    
    public int getPointsEarned() {
        return pointsEarned;
    }
    
    public String getMessage() {
        return message;
    }
}
