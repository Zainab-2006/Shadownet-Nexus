package com.shadownet.nexus.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Table(name = "story_scenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryScene {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    @JsonIgnore
    private StoryChapter chapter;
    
    @Column(nullable = false)
    private Integer sceneNumber;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    private String sceneType;
    
    @Column(nullable = false)
    private String characterSpeaking;
    
    @Column
    private String operatorPovVariants;
    
    @ElementCollection
    @CollectionTable(name = "story_scene_choices", joinColumns = @JoinColumn(name = "scene_id"))
    private List<SceneChoice> choices;
    
    @Column(name = "next_scene_id")
    private Long nextSceneId;
    
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SceneChoice {
        private Long id;
        @Column(columnDefinition = "TEXT")
        private String text;
        @Column(name = "trust_impact")
        private Integer trustImpact;
        @Column(name = "next_scene_id")
        private Long nextSceneId;
    }
}

