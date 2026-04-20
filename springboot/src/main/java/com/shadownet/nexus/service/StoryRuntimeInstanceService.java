package com.shadownet.nexus.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shadownet.nexus.dto.*;
import com.shadownet.nexus.entity.StoryChoice;
import com.shadownet.nexus.entity.StoryInstance;
import com.shadownet.nexus.repository.StoryChoiceRepository;
import com.shadownet.nexus.repository.StoryInstanceRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class StoryRuntimeInstanceService {
    private static final String ACTIVE = "ACTIVE";
    private static final String COMPLETED = "COMPLETED";

    private final StoryInstanceRepository storyRepository;
    private final StoryChoiceRepository choiceRepository;
    private final StoryAwardService awardService;
    private final ObjectMapper objectMapper;

    public StoryRuntimeInstanceService(
            StoryInstanceRepository storyRepository,
            StoryChoiceRepository choiceRepository,
            StoryAwardService awardService,
            ObjectMapper objectMapper) {
        this.storyRepository = storyRepository;
        this.choiceRepository = choiceRepository;
        this.awardService = awardService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public StoryViewDTO startOrReuseStory(String userId, StoryStartRequest request) {
        String operatorCode = requireValue(request.getOperatorCode(), "operatorCode");
        var existing = storyRepository.findFirstByUserIdAndOperatorCodeAndStatus(userId, operatorCode, ACTIVE);
        if (existing.isPresent()) {
            return toViewDto(existing.get());
        }

        String chapterCode = safe(request.getChapterCode(), "chapter-1");
        String sceneCode = safe(request.getSceneCode(), "scene-1");
        long seed = SeedUtil.seedFrom(userId, operatorCode, chapterCode, sceneCode, String.valueOf(System.currentTimeMillis()));

        StoryInstance story = new StoryInstance();
        story.setInstanceKey(SeedUtil.instanceKey("story", seed, operatorCode));
        story.setUserId(userId);
        story.setOperatorCode(operatorCode);
        story.setChapterCode(chapterCode);
        story.setSceneCode(sceneCode);
        story.setTrustLevel(0);
        story.setAffinityLevel(0);
        story.setStatus(ACTIVE);
        story.setChoicesCount(0);
        story.setCreatedAt(LocalDateTime.now());
        storyRepository.save(story);
        return toViewDto(story);
    }

    public StoryViewDTO getStory(String userId, String instanceKey) {
        return toViewDto(getOwned(userId, instanceKey));
    }

    @Transactional
    public StoryDecisionResponse submitChoice(String userId, StoryDecisionRequest request) {
        StoryInstance story = getOwned(userId, requireValue(request.getInstanceKey(), "instanceKey"));
        if (!ACTIVE.equals(story.getStatus())) {
            return decisionResponse(false, "Story route is not active", story, story.getSceneCode());
        }

        String choiceKey = requireValue(request.getChoiceKey(), "choiceKey");
        String option = safe(request.getChosenOption(), choiceKey).toLowerCase();
        int trustDelta;
        int affinityDelta;
        String nextScene;
        switch (option) {
            case "trust" -> { trustDelta = 2; affinityDelta = 1; nextScene = nextScene(story.getSceneCode(), "trust"); }
            case "investigate" -> { trustDelta = 1; affinityDelta = 0; nextScene = nextScene(story.getSceneCode(), "investigate"); }
            case "ignore" -> { trustDelta = -1; affinityDelta = -1; nextScene = nextScene(story.getSceneCode(), "ignore"); }
            case "betray" -> { trustDelta = -3; affinityDelta = -2; nextScene = nextScene(story.getSceneCode(), "betray"); }
            default -> { trustDelta = 0; affinityDelta = 0; nextScene = nextScene(story.getSceneCode(), "observe"); }
        }

        story.setChoicesCount(story.getChoicesCount() + 1);
        story.setTrustLevel(story.getTrustLevel() + trustDelta);
        story.setAffinityLevel(story.getAffinityLevel() + affinityDelta);
        story.setSceneCode(nextScene);
        if (story.getChoicesCount() >= 5 || nextScene.endsWith("-end")) {
            story.setStatus(COMPLETED);
            story.setCompletedAt(LocalDateTime.now());
            awardService.awardStoryProgress(userId, story.getInstanceKey(), 50);
        }

        StoryChoice choice = new StoryChoice();
        choice.setStoryInstanceId(story.getId());
        choice.setChoiceKey(choiceKey);
        choice.setChosenOption(option);
        choice.setTrustDelta(trustDelta);
        choice.setAffinityDelta(affinityDelta);
        choice.setUnlockScene(nextScene);
        choice.setRewardJson(toJson(Map.of("nextScene", nextScene, "trustDelta", trustDelta, "affinityDelta", affinityDelta)));
        choiceRepository.save(choice);
        storyRepository.save(story);
        awardService.recordStoryChoice(userId, story.getInstanceKey(), choiceKey);
        return decisionResponse(true, "Choice applied", story, nextScene);
    }

    private StoryInstance getOwned(String userId, String instanceKey) {
        StoryInstance story = storyRepository.findByInstanceKey(instanceKey)
                .orElseThrow(() -> new IllegalArgumentException("Story instance not found"));
        if (!story.getUserId().equals(userId)) {
            throw new IllegalStateException("You do not own this story instance");
        }
        return story;
    }

    private StoryViewDTO toViewDto(StoryInstance story) {
        StoryViewDTO dto = new StoryViewDTO();
        dto.setInstanceKey(story.getInstanceKey());
        dto.setOperatorCode(story.getOperatorCode());
        dto.setChapterCode(story.getChapterCode());
        dto.setSceneCode(story.getSceneCode());
        dto.setTitle("Operator " + story.getOperatorCode() + " - " + story.getSceneCode());
        dto.setSceneText("This backend-authored scene continues " + story.getOperatorCode()
                + " in " + story.getChapterCode() + ". Current scene: " + story.getSceneCode() + ".");
        dto.setTrustLevel(story.getTrustLevel());
        dto.setAffinityLevel(story.getAffinityLevel());
        dto.setChoicesCount(story.getChoicesCount());
        dto.setStatus(story.getStatus());
        dto.setChoices(List.of(
                choice("trust", "Trust the operator"),
                choice("investigate", "Investigate the signal"),
                choice("ignore", "Ignore the anomaly")));
        return dto;
    }

    private StoryChoiceOptionDTO choice(String key, String label) {
        StoryChoiceOptionDTO dto = new StoryChoiceOptionDTO();
        dto.setChoiceKey(key);
        dto.setLabel(label);
        return dto;
    }

    private StoryDecisionResponse decisionResponse(boolean success, String message, StoryInstance story, String nextScene) {
        StoryDecisionResponse response = new StoryDecisionResponse();
        response.setSuccess(success);
        response.setMessage(message);
        response.setNextSceneCode(nextScene);
        response.setTrustLevel(story.getTrustLevel());
        response.setAffinityLevel(story.getAffinityLevel());
        response.setStatus(story.getStatus());
        return response;
    }

    private String nextScene(String currentScene, String branch) {
        if (currentScene.endsWith("-end")) return currentScene;
        return currentScene + "-" + branch;
    }

    private String requireValue(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize story JSON", ex);
        }
    }
}
