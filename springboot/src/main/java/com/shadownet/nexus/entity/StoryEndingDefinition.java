package com.shadownet.nexus.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "story_ending_definitions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryEndingDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ending_key", nullable = false, unique = true, length = 40)
    private String endingKey;

    @Column(name = "ending_title", nullable = false)
    private String endingTitle;

    @Column(name = "ending_description", columnDefinition = "TEXT")
    private String endingDescription;

    @Column(name = "minimum_trust", nullable = false)
    @Builder.Default
    private Integer minimumTrust = 0;

    @Column(name = "required_choices", columnDefinition = "JSON")
    private String requiredChoices;

    @Column(name = "ending_type", nullable = false, length = 40)
    @Builder.Default
    private String endingType = "standard";

    @Column(name = "unlock_rank", nullable = false)
    @Builder.Default
    private Integer unlockRank = 0;
}
