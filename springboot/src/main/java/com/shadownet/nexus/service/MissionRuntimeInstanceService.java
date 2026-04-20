package com.shadownet.nexus.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shadownet.nexus.dto.*;
import com.shadownet.nexus.entity.*;
import com.shadownet.nexus.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class MissionRuntimeInstanceService {
    private static final String ACTIVE = "ACTIVE";
    private static final String COMPLETED = "COMPLETED";
    private static final String EXPIRED = "EXPIRED";

    private final MissionInstanceRepository missionRepository;
    private final MissionEvidenceRepository evidenceRepository;
    private final MissionDecisionRepository decisionRepository;
    private final MissionAwardService awardService;
    private final EventService eventService;
    private final ObjectMapper objectMapper;

    public MissionRuntimeInstanceService(
            MissionInstanceRepository missionRepository,
            MissionEvidenceRepository evidenceRepository,
            MissionDecisionRepository decisionRepository,
            MissionAwardService awardService,
            EventService eventService,
            ObjectMapper objectMapper) {
        this.missionRepository = missionRepository;
        this.evidenceRepository = evidenceRepository;
        this.decisionRepository = decisionRepository;
        this.awardService = awardService;
        this.eventService = eventService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public MissionViewDTO startOrReuseMission(String userId, MissionStartRequest request) {
        String missionCode = requireValue(request.getMissionCode(), "missionCode");
        var existing = missionRepository.findFirstByOwnerUserIdAndMissionCodeAndStatus(userId, missionCode, ACTIVE);
        if (existing.isPresent() && !isExpired(existing.get())) {
            return toViewDto(existing.get());
        }
        existing.filter(this::isExpired).ifPresent(expired -> {
            expired.setStatus(EXPIRED);
            missionRepository.save(expired);
        });

        long seed = SeedUtil.seedFrom(userId, missionCode, safe(request.getSquadId(), "solo"), String.valueOf(System.currentTimeMillis()));
        MissionInstance mission = new MissionInstance();
        mission.setInstanceKey(SeedUtil.instanceKey("mission", seed, missionCode));
        mission.setMissionCode(missionCode);
        mission.setOwnerUserId(userId);
        mission.setSquadId(request.getSquadId());
        mission.setStatus(ACTIVE);
        mission.setPhase("BRIEFING");
        mission.setCreatedAt(LocalDateTime.now());
        mission.setExpiresAt(LocalDateTime.now().plusHours(4));
        missionRepository.save(mission);
        seedEvidence(mission);
        eventService.logEvent(userId, "mission_runtime_started", mission.getInstanceKey(), "mission", Map.of("missionCode", missionCode));
        return toViewDto(mission);
    }

    public MissionViewDTO getMission(String userId, String instanceKey) {
        MissionInstance mission = getOwned(userId, instanceKey);
        if (ACTIVE.equals(mission.getStatus()) && isExpired(mission)) {
            mission.setStatus(EXPIRED);
            missionRepository.save(mission);
        }
        return toViewDto(mission);
    }

    @Transactional
    public MissionDecisionResponse submitDecision(String userId, MissionDecisionRequest request) {
        MissionInstance mission = getOwned(userId, requireValue(request.getInstanceKey(), "instanceKey"));
        if (!ACTIVE.equals(mission.getStatus())) {
            return decisionResponse(false, "Mission is not active", mission);
        }
        if (isExpired(mission)) {
            mission.setStatus(EXPIRED);
            missionRepository.save(mission);
            return decisionResponse(false, "Mission expired", mission);
        }

        String option = safe(request.getChosenOption(), "observe").toLowerCase();
        int trustDelta;
        int suspicionDelta;
        int creditsDelta;
        String nextPhase;
        switch (option) {
            case "trust" -> { trustDelta = 2; suspicionDelta = -1; creditsDelta = 10; nextPhase = "ANALYSIS"; }
            case "verify" -> { trustDelta = 1; suspicionDelta = 0; creditsDelta = 15; nextPhase = "EVIDENCE"; }
            case "suspect" -> { trustDelta = -1; suspicionDelta = 2; creditsDelta = 0; nextPhase = "REVIEW"; }
            case "accuse" -> { trustDelta = -2; suspicionDelta = 3; creditsDelta = -20; nextPhase = "RESOLUTION"; }
            default -> { trustDelta = 0; suspicionDelta = 1; creditsDelta = -5; nextPhase = mission.getPhase(); }
        }

        mission.setAttemptCount(mission.getAttemptCount() + 1);
        mission.setDecisionsCount(mission.getDecisionsCount() + 1);
        mission.setTrustScore(mission.getTrustScore() + trustDelta);
        mission.setSuspicionScore(mission.getSuspicionScore() + suspicionDelta);
        mission.setCredits(mission.getCredits() + creditsDelta);
        mission.setPhase(nextPhase);

        MissionDecision decision = new MissionDecision();
        decision.setMissionInstanceId(mission.getId());
        decision.setDecisionKey(requireValue(request.getDecisionKey(), "decisionKey"));
        decision.setChosenOption(option);
        decision.setTrustDelta(trustDelta);
        decision.setSuspicionDelta(suspicionDelta);
        decision.setCreditsDelta(creditsDelta);
        decision.setConsequenceJson(toJson(Map.of("phase", nextPhase, "trustDelta", trustDelta, "suspicionDelta", suspicionDelta, "creditsDelta", creditsDelta)));
        decisionRepository.save(decision);

        if ("RESOLUTION".equals(nextPhase) || mission.getDecisionsCount() >= 5) {
            mission.setStatus(COMPLETED);
            mission.setCompletedAt(LocalDateTime.now());
            awardService.awardMissionCompletion(userId, mission.getInstanceKey(), Math.max(0, mission.getCredits()), Math.max(0, mission.getTrustScore()) * 10);
        }
        missionRepository.save(mission);
        return decisionResponse(true, "Decision applied", mission);
    }

    @Transactional
    public MissionViewDTO claimEvidence(String userId, MissionEvidenceClaimRequest request) {
        MissionInstance mission = getOwned(userId, requireValue(request.getInstanceKey(), "instanceKey"));
        MissionEvidence evidence = evidenceRepository
                .findByMissionInstanceIdAndEvidenceKey(mission.getId(), requireValue(request.getEvidenceKey(), "evidenceKey"))
                .orElseThrow(() -> new IllegalArgumentException("Evidence not found"));
        if (!Boolean.TRUE.equals(evidence.getFound())) {
            evidence.setFound(true);
            evidenceRepository.save(evidence);
            mission.setCredits(mission.getCredits() + 5);
            mission.setTrustScore(mission.getTrustScore() + 1);
            missionRepository.save(mission);
        }
        return toViewDto(mission);
    }

    private void seedEvidence(MissionInstance mission) {
        evidenceRepository.saveAll(List.of(
                evidence(mission.getId(), "ev_log_01", "log", Map.of("title", "Access Log", "detail", "Suspicious login window detected.")),
                evidence(mission.getId(), "ev_file_01", "file", Map.of("title", "Encrypted File", "detail", "Recovered archive requires operator review.")),
                evidence(mission.getId(), "ev_chat_01", "chat", Map.of("title", "Intercepted Chat", "detail", "Messages suggest coordinated insider movement."))
        ));
    }

    private MissionEvidence evidence(Long missionId, String key, String type, Map<String, String> content) {
        MissionEvidence evidence = new MissionEvidence();
        evidence.setMissionInstanceId(missionId);
        evidence.setEvidenceKey(key);
        evidence.setEvidenceType(type);
        evidence.setFound(false);
        evidence.setContentJson(toJson(content));
        evidence.setCreatedAt(LocalDateTime.now());
        return evidence;
    }

    private MissionInstance getOwned(String userId, String instanceKey) {
        MissionInstance mission = missionRepository.findByInstanceKey(instanceKey)
                .orElseThrow(() -> new IllegalArgumentException("Mission instance not found"));
        if (!mission.getOwnerUserId().equals(userId)) {
            throw new IllegalStateException("You do not own this mission instance");
        }
        return mission;
    }

    private MissionViewDTO toViewDto(MissionInstance mission) {
        MissionViewDTO dto = new MissionViewDTO();
        dto.setInstanceKey(mission.getInstanceKey());
        dto.setMissionCode(mission.getMissionCode());
        dto.setStatus(mission.getStatus());
        dto.setPhase(mission.getPhase());
        dto.setTrustScore(mission.getTrustScore());
        dto.setSuspicionScore(mission.getSuspicionScore());
        dto.setCredits(mission.getCredits());
        dto.setDecisionsCount(mission.getDecisionsCount());
        dto.setVisibleEvidence(evidenceRepository.findByMissionInstanceId(mission.getId()).stream().map(this::toEvidenceDto).toList());
        return dto;
    }

    private MissionEvidenceViewDTO toEvidenceDto(MissionEvidence evidence) {
        MissionEvidenceViewDTO dto = new MissionEvidenceViewDTO();
        dto.setEvidenceKey(evidence.getEvidenceKey());
        dto.setEvidenceType(evidence.getEvidenceType());
        dto.setFound(Boolean.TRUE.equals(evidence.getFound()));
        dto.setContentJson(Boolean.TRUE.equals(evidence.getFound()) ? evidence.getContentJson() : null);
        return dto;
    }

    private MissionDecisionResponse decisionResponse(boolean success, String message, MissionInstance mission) {
        MissionDecisionResponse response = new MissionDecisionResponse();
        response.setSuccess(success);
        response.setMessage(message);
        response.setTrustScore(mission.getTrustScore());
        response.setSuspicionScore(mission.getSuspicionScore());
        response.setCredits(mission.getCredits());
        response.setPhase(mission.getPhase());
        response.setStatus(mission.getStatus());
        return response;
    }

    private boolean isExpired(MissionInstance mission) {
        return mission.getExpiresAt() != null && mission.getExpiresAt().isBefore(LocalDateTime.now());
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
            throw new IllegalStateException("Failed to serialize mission JSON", ex);
        }
    }
}
