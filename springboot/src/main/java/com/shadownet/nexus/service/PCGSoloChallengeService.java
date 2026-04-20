package com.shadownet.nexus.service;

import com.shadownet.nexus.dto.PCGChallengeGenerateRequest;
import com.shadownet.nexus.dto.PCGChallengeSubmitRequest;
import com.shadownet.nexus.dto.PCGChallengeSubmitResponse;
import com.shadownet.nexus.dto.PCGChallengeViewDTO;
import com.shadownet.nexus.entity.PCGChallengeInstance;
import com.shadownet.nexus.repository.PCGChallengeInstanceRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@Service
public class PCGSoloChallengeService {

    private static final String ACTIVE = "ACTIVE";
    private static final String EXPIRED = "EXPIRED";
    private static final String SOLVED = "SOLVED";

    private final PCGChallengeInstanceRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final ScoreAwardService scoreAwardService;
    private final EventService eventService;
    private final AdaptiveEngineService adaptiveEngineService;

    public PCGSoloChallengeService(
            PCGChallengeInstanceRepository repository,
            PasswordEncoder passwordEncoder,
            ScoreAwardService scoreAwardService,
            EventService eventService,
            AdaptiveEngineService adaptiveEngineService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.scoreAwardService = scoreAwardService;
        this.eventService = eventService;
        this.adaptiveEngineService = adaptiveEngineService;
    }

    @Transactional
    public PCGChallengeViewDTO generateOrReuseChallenge(String userId, PCGChallengeGenerateRequest request) {
        String sessionId = requireValue(request.getSessionId(), "sessionId");

        var existing = repository.findFirstByUserIdAndSessionIdAndStatus(userId, sessionId, ACTIVE);
        if (existing.isPresent()) {
            PCGChallengeInstance active = existing.get();
            if (active.getExpiresAt() == null || active.getExpiresAt().isAfter(LocalDateTime.now())) {
                return toViewDto(active);
            }
            active.setStatus(EXPIRED);
            repository.save(active);
        }

        long seed = SeedUtil.seedFrom(
                userId,
                sessionId,
                safe(request.getCategory(), "auto"),
                safe(request.getDifficulty(), "auto"),
                String.valueOf(System.currentTimeMillis()));
        Random random = SeedUtil.seededRandom(seed);

        String category = sanitizeChoice(request.getCategory(), pick(random, "web", "crypto", "forensics", "rev", "osint", "misc"));
        String difficulty = sanitizeChoice(request.getDifficulty(), pick(random, "easy", "medium", "hard"));
        int points = pointsFor(difficulty);
        String flag = "CTF{pcg_" + category + "_" + Math.abs(seed % 100000) + "}";

        PCGChallengeInstance entity = new PCGChallengeInstance();
        entity.setInstanceKey(SeedUtil.instanceKey("solo", seed, sessionId));
        entity.setUserId(userId);
        entity.setSessionId(sessionId);
        entity.setSeed(seed);
        entity.setMode("solo");
        entity.setCategory(category);
        entity.setDifficulty(difficulty);
        entity.setTitle("Dynamic " + capitalize(category) + " Challenge");
        entity.setDescription("Solve this backend-generated " + difficulty + " " + category
                + " challenge instance. The answer is validated only by the server.");
        entity.setArtifactJson("{\"seed\":" + seed + ",\"category\":\"" + category + "\",\"difficulty\":\"" + difficulty + "\"}");
        entity.setFlagHash(passwordEncoder.encode(flag));
        entity.setPoints(points);
        entity.setStatus(ACTIVE);
        entity.setAttemptCount(0);
        entity.setHintsUsed(0);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setExpiresAt(LocalDateTime.now().plusHours(2));

        repository.save(entity);
        eventService.logEvent(userId, "pcg_solo_generated", entity.getInstanceKey(), category,
                Map.of("difficulty", difficulty, "points", points));
        return toViewDto(entity);
    }

    public PCGChallengeViewDTO getChallenge(String userId, String instanceKey) {
        PCGChallengeInstance entity = getOwnedInstance(userId, instanceKey);
        return toViewDto(entity);
    }

    @Transactional
    public PCGChallengeSubmitResponse submitChallenge(String userId, PCGChallengeSubmitRequest request) {
        String instanceKey = requireValue(request.getInstanceKey(), "instanceKey");
        String submittedFlag = requireValue(request.getSubmittedFlag(), "submittedFlag");
        PCGChallengeInstance entity = getOwnedInstance(userId, instanceKey);

        if (SOLVED.equalsIgnoreCase(entity.getStatus())) {
            return response(true, "Challenge already solved", 0, entity.getStatus());
        }

        if (entity.getExpiresAt() != null && entity.getExpiresAt().isBefore(LocalDateTime.now())) {
            entity.setStatus(EXPIRED);
            repository.save(entity);
            return response(false, "Challenge expired", 0, EXPIRED);
        }

        entity.setAttemptCount(entity.getAttemptCount() + 1);
        boolean correct = passwordEncoder.matches(submittedFlag, entity.getFlagHash());
        eventService.logEvent(userId, correct ? "pcg_solo_correct" : "pcg_solo_wrong", instanceKey, entity.getCategory(),
                Map.of("attemptCount", entity.getAttemptCount(), "difficulty", entity.getDifficulty()));

        if (!correct) {
            repository.save(entity);
            adaptiveEngineService.updateSkill(userId, entity.getCategory(), false, 0);
            return response(false, "Incorrect flag", 0, entity.getStatus());
        }

        entity.setStatus(SOLVED);
        entity.setSolvedAt(LocalDateTime.now());
        repository.save(entity);
        scoreAwardService.awardSoloPCGPoints(userId, entity.getPoints());
        adaptiveEngineService.updateSkill(userId, entity.getCategory(), true, 0);
        return response(true, "Correct flag", entity.getPoints(), SOLVED);
    }

    private PCGChallengeInstance getOwnedInstance(String userId, String instanceKey) {
        PCGChallengeInstance entity = repository.findByInstanceKey(instanceKey)
                .orElseThrow(() -> new IllegalArgumentException("Challenge instance not found"));
        if (!entity.getUserId().equals(userId)) {
            throw new IllegalStateException("You do not own this challenge instance");
        }
        return entity;
    }

    private PCGChallengeViewDTO toViewDto(PCGChallengeInstance entity) {
        PCGChallengeViewDTO dto = new PCGChallengeViewDTO();
        dto.setInstanceKey(entity.getInstanceKey());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setCategory(entity.getCategory());
        dto.setDifficulty(entity.getDifficulty());
        dto.setPoints(entity.getPoints());
        dto.setArtifactJson(entity.getArtifactJson());
        dto.setAttemptCount(entity.getAttemptCount());
        dto.setHintsUsed(entity.getHintsUsed());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    private PCGChallengeSubmitResponse response(boolean correct, String message, Integer pointsAwarded, String status) {
        PCGChallengeSubmitResponse response = new PCGChallengeSubmitResponse();
        response.setCorrect(correct);
        response.setMessage(message);
        response.setPointsAwarded(pointsAwarded);
        response.setStatus(status);
        return response;
    }

    private String requireValue(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim().toLowerCase();
    }

    private String sanitizeChoice(String value, String fallback) {
        String next = safe(value, fallback).replaceAll("[^a-z0-9_-]", "");
        return next.isBlank() ? fallback : next;
    }

    private int pointsFor(String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "easy" -> 100;
            case "medium" -> 200;
            case "hard" -> 400;
            default -> 150;
        };
    }

    private String pick(Random random, String... items) {
        return items[random.nextInt(items.length)];
    }

    private String capitalize(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
}
