package com.shadownet.nexus.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_mission_state",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_mission_state", columnNames = {"user_id", "mission_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMissionState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "mission_id", nullable = false, length = 120)
    private String missionId;

    @Column(nullable = false, length = 40)
    private String state;

    @Column(name = "source_chapter_id")
    private Long sourceChapterId;

    @Column(name = "source_scene_id")
    private Long sourceSceneId;

    @Column(name = "source_choice_id")
    private Long sourceChoiceId;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
