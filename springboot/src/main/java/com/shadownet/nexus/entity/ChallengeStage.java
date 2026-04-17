package com.shadownet.nexus.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "challenge_stages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeStage {
    @Id
    private String id;

    @Column(name = "challenge_id", nullable = false)
    private String challengeId;

    @Column(name = "stage_number", nullable = false)
    private int stageNumber;

    @Column(nullable = false)
    private String stageType;

    private String description;

    @Column(name = "flag_hash")
    private String flagHash;

    private String hint;

    private Integer points;

    private String briefing;

    private String learning;

    @Column(name = "created_at")
    private Long createdAt;
}
