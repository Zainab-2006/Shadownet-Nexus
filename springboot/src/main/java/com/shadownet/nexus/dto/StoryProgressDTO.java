package com.shadownet.nexus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryProgressDTO {

    private String userId;

    private Long currentChapterId;

    private Long currentSceneId;

    private List<Long> completedChapters;

    private Map<Long, Long> choicesMade;

    private int completionPercentage;
}
