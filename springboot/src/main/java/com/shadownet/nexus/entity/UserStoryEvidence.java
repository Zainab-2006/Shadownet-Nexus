package com.shadownet.nexus.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_story_evidence",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_story_evidence_code", columnNames = {"user_id", "evidence_code"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStoryEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "evidence_code", nullable = false, length = 120)
    private String evidenceCode;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "source_chapter_id")
    private Long sourceChapterId;

    @Column(name = "source_scene_id")
    private Long sourceSceneId;

    @Column(name = "source_choice_id")
    private Long sourceChoiceId;

    @Column(name = "operator_interpretation", columnDefinition = "TEXT")
    private String operatorInterpretation;

    @Column(name = "mission_relevance_tag", length = 120)
    private String missionRelevanceTag;

    @Column(name = "discovered_at", nullable = false)
    private LocalDateTime discoveredAt;
}
