package com.shadownet.nexus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "story_choices")
public class StoryChoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "story_instance_id", nullable = false)
    private Long storyInstanceId;

    @Column(name = "choice_key", nullable = false, length = 100)
    private String choiceKey;

    @Column(name = "chosen_option", nullable = false, length = 100)
    private String chosenOption;

    @Column(name = "trust_delta", nullable = false)
    private Integer trustDelta = 0;

    @Column(name = "affinity_delta", nullable = false)
    private Integer affinityDelta = 0;

    @Column(name = "unlock_scene", length = 100)
    private String unlockScene;

    @Column(name = "reward_json", columnDefinition = "LONGTEXT")
    private String rewardJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public Long getStoryInstanceId() { return storyInstanceId; }
    public void setStoryInstanceId(Long storyInstanceId) { this.storyInstanceId = storyInstanceId; }
    public String getChoiceKey() { return choiceKey; }
    public void setChoiceKey(String choiceKey) { this.choiceKey = choiceKey; }
    public String getChosenOption() { return chosenOption; }
    public void setChosenOption(String chosenOption) { this.chosenOption = chosenOption; }
    public Integer getTrustDelta() { return trustDelta; }
    public void setTrustDelta(Integer trustDelta) { this.trustDelta = trustDelta; }
    public Integer getAffinityDelta() { return affinityDelta; }
    public void setAffinityDelta(Integer affinityDelta) { this.affinityDelta = affinityDelta; }
    public String getUnlockScene() { return unlockScene; }
    public void setUnlockScene(String unlockScene) { this.unlockScene = unlockScene; }
    public String getRewardJson() { return rewardJson; }
    public void setRewardJson(String rewardJson) { this.rewardJson = rewardJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
