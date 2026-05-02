package com.shadownet.nexus.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "story_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryProgress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "current_chapter_id")
    private Long currentChapterId;
    
    @Column(name = "current_scene_id")
    private Long currentSceneId;
    
    @Column(name = "completed_chapters", columnDefinition = "JSON")
    @Builder.Default
    private String completedChaptersJson = "[]";
    
    @Column(name = "choices_made", columnDefinition = "JSON")
    @Builder.Default
    private String choicesMadeJson = "{}";
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "ending_achieved", length = 40)
    private String endingAchieved;
    
    @Transient
    @Builder.Default
    private List<Long> completedChapters = new ArrayList<>();
    
    @Transient
    @Builder.Default
    private Map<Long, Long> choicesMade = new HashMap<>();
    
    @PostLoad
    public void loadJsonData() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            completedChapters = mapper.readValue(completedChaptersJson, new TypeReference<List<Long>>() {});
            choicesMade = mapper.readValue(choicesMadeJson, new TypeReference<Map<Long, Long>>() {});
        } catch (Exception e) {
            // Default empty
        }
    }
    
    @PrePersist
    @PreUpdate
    public void saveJsonData() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            completedChaptersJson = mapper.writeValueAsString(completedChapters);
            choicesMadeJson = mapper.writeValueAsString(choicesMade);
        } catch (Exception e) {
            completedChaptersJson = "[]";
            choicesMadeJson = "{}";
        }
    }
}

