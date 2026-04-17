package com.shadownet.nexus.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "challenge_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeSession {
    @Id
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "challenge_id", nullable = false)
    private String challengeId;

    private String operator;

    private String difficulty;

    @Column(name = "current_stage", nullable = false)
    private int currentStage = 1;

    private String status = "active";

    @Column(name = "time_started")
    private Long timeStarted;

    @Column(name = "time_completed")
    private Long timeCompleted;

    @Column(name = "total_time")
    private Integer totalTime;

    @Column(name = "hints_used")
    private int hintsUsed = 0;

    @Column(name = "penalty_multiplier")
    private Double penaltyMultiplier = 1.0;

    private Boolean completed = false;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;
}
