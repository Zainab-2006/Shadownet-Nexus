package com.shadownet.nexus.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shadownet.nexus.dto.ErrorResponse;
import com.shadownet.nexus.entity.Mission;
import com.shadownet.nexus.entity.MissionSession;
import com.shadownet.nexus.entity.User;
import com.shadownet.nexus.repository.MissionRepository;
import com.shadownet.nexus.repository.MissionSessionRepository;
import com.shadownet.nexus.repository.UserRepository;
import com.shadownet.nexus.service.GameplayConsequenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class MissionController {

    @Autowired
    private MissionRepository missionRepository;

    @Autowired
    private MissionSessionRepository missionSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameplayConsequenceService gameplayConsequenceService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/missions")
    public ResponseEntity<List<Mission>> getMissions() {
        return ResponseEntity.ok(missionRepository.findAll());
    }

    @GetMapping("/missions/progress")
    public ResponseEntity<?> getMissionProgress(Authentication auth) {
        return ResponseEntity.ok(gameplayConsequenceService.getMissionStates(auth.getName()));
    }

    @GetMapping("/missions/{id}")
    public ResponseEntity<Mission> getMission(@PathVariable String id) {
        Mission mission = missionRepository.findById(id).orElse(null);
        if (mission == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mission);
    }

    @GetMapping("/missions/{id}/runtime")
    public ResponseEntity<?> getMissionRuntime(@PathVariable String id, Authentication auth) {
        Mission mission = missionRepository.findById(id).orElse(null);
        if (mission == null) {
            return ResponseEntity.notFound().build();
        }

        User user = requireUser(auth.getName());
        MissionSession session = missionSessionRepository
                .findFirstByUserIdAndMissionIdOrderByStartedAtDesc(user.getId(), id)
                .orElseGet(() -> createRuntimeSession(user, mission));
        return ResponseEntity.ok(toRuntimeResponse(session, mission));
    }

    @PostMapping("/missions/{id}/runtime/start")
    public ResponseEntity<?> startMissionRuntime(@PathVariable String id, Authentication auth) {
        Mission mission = missionRepository.findById(id).orElse(null);
        if (mission == null) {
            return ResponseEntity.notFound().build();
        }

        User user = requireUser(auth.getName());
        MissionSession session = missionSessionRepository
                .findFirstByUserIdAndMissionIdOrderByStartedAtDesc(user.getId(), id)
                .filter(existing -> !"completed".equalsIgnoreCase(existing.getStatus()))
                .orElseGet(() -> createRuntimeSession(user, mission));
        session.setStatus("active");
        session.setEndedAt(null);
        missionSessionRepository.save(session);
        gameplayConsequenceService.applyMissionAction(auth.getName(), id, "START");
        return ResponseEntity.ok(toRuntimeResponse(session, mission));
    }

    @PostMapping("/missions/{id}/runtime/objective")
    public ResponseEntity<?> updateMissionObjective(
            @PathVariable String id,
            @RequestBody Map<String, Object> body,
            Authentication auth) {
        Mission mission = missionRepository.findById(id).orElse(null);
        if (mission == null) {
            return ResponseEntity.notFound().build();
        }

        User user = requireUser(auth.getName());
        MissionSession session = missionSessionRepository
                .findFirstByUserIdAndMissionIdOrderByStartedAtDesc(user.getId(), id)
                .orElseGet(() -> createRuntimeSession(user, mission));
        if (!"active".equalsIgnoreCase(session.getStatus())) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    "MISSION_NOT_ACTIVE",
                    "Mission objectives can only change while the runtime session is active.",
                    400));
        }

        String objectiveId = body == null ? null : String.valueOf(body.get("objectiveId"));
        if (objectiveId == null || objectiveId.isBlank() || "null".equals(objectiveId)) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    "OBJECTIVE_ID_REQUIRED",
                    "objectiveId is required.",
                    400));
        }

        Map<String, Object> progress = parseProgress(session, mission);
        List<Map<String, Object>> objectives = getObjectiveProgress(progress);
        boolean updated = false;
        for (Map<String, Object> objective : objectives) {
            if (objectiveId.equals(String.valueOf(objective.get("id")))) {
                Object requestedComplete = body.get("complete");
                boolean current = Boolean.TRUE.equals(objective.get("complete"));
                objective.put("complete", requestedComplete instanceof Boolean ? requestedComplete : !current);
                updated = true;
                break;
            }
        }

        if (!updated) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    "OBJECTIVE_NOT_FOUND",
                    "Mission objective was not found in this runtime session.",
                    400));
        }

        progress.put("objectives", objectives);
        writeProgress(session, progress);
        missionSessionRepository.save(session);
        return ResponseEntity.ok(toRuntimeResponse(session, mission));
    }

    @PostMapping("/missions/{id}/runtime/complete")
    public ResponseEntity<?> completeMissionRuntime(@PathVariable String id, Authentication auth) {
        Mission mission = missionRepository.findById(id).orElse(null);
        if (mission == null) {
            return ResponseEntity.notFound().build();
        }

        User user = requireUser(auth.getName());
        MissionSession session = missionSessionRepository
                .findFirstByUserIdAndMissionIdOrderByStartedAtDesc(user.getId(), id)
                .orElseGet(() -> createRuntimeSession(user, mission));
        Map<String, Object> progress = parseProgress(session, mission);
        List<Map<String, Object>> objectives = getObjectiveProgress(progress);
        boolean allComplete = !objectives.isEmpty() && objectives.stream()
                .allMatch(objective -> Boolean.TRUE.equals(objective.get("complete")));
        if (!allComplete) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    "MISSION_OBJECTIVES_INCOMPLETE",
                    "All mission objectives must be complete before completion can be recorded.",
                    400));
        }

        session.setStatus("completed");
        session.setEndedAt(System.currentTimeMillis());
        missionSessionRepository.save(session);
        Map<String, Object> response = toRuntimeResponse(session, mission);
        response.put("missionConsequence", gameplayConsequenceService.applyMissionAction(auth.getName(), id, "COMPLETE"));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/missions/{id}/progress")
    public ResponseEntity<?> updateMissionProgress(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(new ErrorResponse(
                        "MISSION_PROGRESS_DEPRECATED",
                        "Client-authored mission progress is retired. Use backend-authored consequence action endpoints.",
                        410));
    }

    @PostMapping("/missions/{id}/action")
    public ResponseEntity<?> applyMissionAction(@PathVariable String id,
            @RequestBody Map<String, Object> body,
            Authentication auth) {
        String action = body == null ? null : String.valueOf(body.getOrDefault("action", "RECOMMEND"));
        String normalizedAction = action == null ? "RECOMMEND" : action.trim().toUpperCase(Locale.ROOT);
        if (!List.of("START", "UNLOCK", "COMPLETE", "RECOMMEND").contains(normalizedAction)) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    "MISSION_ACTION_UNSUPPORTED",
                    "Mission action must be START, UNLOCK, COMPLETE, or RECOMMEND.",
                    400));
        }
        return ResponseEntity.ok(gameplayConsequenceService.applyMissionAction(auth.getName(), id, normalizedAction));
    }

    @GetMapping("/search/missions")
    public ResponseEntity<Map<String, Object>> searchMissions(@RequestParam String q) {
        List<Mission> results = missionRepository.findAll().stream()
                .filter(m -> m.getTitle().toLowerCase().contains(q.toLowerCase()))
                .toList();
        return ResponseEntity.ok(Map.of("query", q, "results", results));
    }

    private MissionSession createRuntimeSession(User user, Mission mission) {
        MissionSession session = new MissionSession();
        session.setId("mission_session_" + UUID.randomUUID());
        session.setUserId(user.getId());
        session.setMissionId(mission.getId());
        session.setStatus("active");
        session.setStartedAt(System.currentTimeMillis());
        writeProgress(session, defaultProgress(mission));
        return missionSessionRepository.save(session);
    }

    private Map<String, Object> toRuntimeResponse(MissionSession session, Mission mission) {
        Map<String, Object> progress = parseProgress(session, mission);
        List<Map<String, Object>> objectives = getObjectiveProgress(progress);
        long startedAt = session.getStartedAt() == null ? System.currentTimeMillis() : session.getStartedAt();
        int timeLimit = mission.getTimeLimitSeconds() == null ? 3600 : mission.getTimeLimitSeconds();
        int elapsed = (int) Math.max(0, (System.currentTimeMillis() - startedAt) / 1000);
        int timeRemaining = Math.max(0, timeLimit - elapsed);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", session.getId());
        response.put("missionId", session.getMissionId());
        response.put("status", session.getStatus());
        response.put("objectives", objectives);
        response.put("timeLimitSeconds", timeLimit);
        response.put("timeRemaining", timeRemaining);
        response.put("evidenceCount", progress.getOrDefault("evidenceCount", 0));
        response.put("startedAt", session.getStartedAt());
        response.put("endedAt", session.getEndedAt());
        response.put("completedObjectives", objectives.stream().filter(objective -> Boolean.TRUE.equals(objective.get("complete"))).count());
        return response;
    }

    private Map<String, Object> defaultProgress(Mission mission) {
        Map<String, Object> progress = new LinkedHashMap<>();
        progress.put("objectives", parseMissionObjectives(mission));
        progress.put("evidenceCount", 0);
        return progress;
    }

    private Map<String, Object> parseProgress(MissionSession session, Mission mission) {
        if (session.getProgress() == null || session.getProgress().isBlank()) {
            return defaultProgress(mission);
        }
        try {
            return objectMapper.readValue(session.getProgress(), new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException ex) {
            return defaultProgress(mission);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getObjectiveProgress(Map<String, Object> progress) {
        Object rawObjectives = progress.get("objectives");
        if (rawObjectives instanceof List<?>) {
            List<Map<String, Object>> objectives = new ArrayList<>();
            for (Object rawObjective : (List<?>) rawObjectives) {
                if (rawObjective instanceof Map<?, ?> rawMap) {
                    Map<String, Object> objective = new LinkedHashMap<>();
                    rawMap.forEach((key, value) -> objective.put(String.valueOf(key), value));
                    objectives.add(objective);
                }
            }
            return objectives;
        }
        return List.of();
    }

    private List<Map<String, Object>> parseMissionObjectives(Mission mission) {
        List<String> titles = new ArrayList<>();
        if (mission.getObjectives() != null && !mission.getObjectives().isBlank()) {
            try {
                titles = objectMapper.readValue(mission.getObjectives(), new TypeReference<List<String>>() {});
            } catch (JsonProcessingException ignored) {
                titles = List.of(mission.getObjectives());
            }
        }
        if (titles.isEmpty()) {
            titles = List.of("Complete the mission briefing.");
        }

        List<Map<String, Object>> objectives = new ArrayList<>();
        for (int index = 0; index < titles.size(); index++) {
            Map<String, Object> objective = new LinkedHashMap<>();
            objective.put("id", "obj" + (index + 1));
            objective.put("title", titles.get(index));
            objective.put("complete", false);
            objectives.add(objective);
        }
        return objectives;
    }

    private void writeProgress(MissionSession session, Map<String, Object> progress) {
        try {
            session.setProgress(objectMapper.writeValueAsString(progress));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize mission runtime progress", ex);
        }
    }

    private User requireUser(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            return user;
        }
        return userRepository.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}