package com.shadownet.nexus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

@Entity
@Table(name = "trust_relationship")
@Getter
@Setter
@ToString
public class TrustEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(name = "target_user_id", nullable = false, length = 128)
    private String targetUserId;

    @Column(name = "trust_score", nullable = false)
    private Integer trustScore = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
