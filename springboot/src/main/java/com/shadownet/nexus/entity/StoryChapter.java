package com.shadownet.nexus.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "story_chapters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryChapter {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private Integer chapterNumber;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Builder.Default
    private boolean isLocked = true;
    
    @Builder.Default
    private int requiredTrustLevel = 0;
    
    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StoryScene> scenes;
}
