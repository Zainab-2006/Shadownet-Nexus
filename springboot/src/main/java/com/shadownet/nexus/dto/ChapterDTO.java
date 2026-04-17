package com.shadownet.nexus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterDTO {
    
    private Long id;
    
    private Integer chapterNumber;
    
    private String title;
    
    private String description;
    
    private boolean isLocked;
    
    private int requiredTrustLevel;
}

