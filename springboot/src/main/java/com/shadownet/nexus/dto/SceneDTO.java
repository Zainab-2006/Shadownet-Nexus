package com.shadownet.nexus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import com.shadownet.nexus.entity.StoryScene;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SceneDTO {
    
    private Long id;
    
    private Long chapterId;
    
    private Integer sceneNumber;
    
    private String sceneType;
    
    private String content;
    
    private String characterSpeaking;
    
    private String operatorPovVariants;
    
    private List<StoryScene.SceneChoice> choices;
    
    private Long nextSceneId;
}

