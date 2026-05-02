package com.shadownet.nexus.service;

import com.shadownet.nexus.dto.MemberViewDTO;
import com.shadownet.nexus.dto.TeamSessionViewDTO;
import com.shadownet.nexus.entity.TeamSession;
import com.shadownet.nexus.entity.User;
import com.shadownet.nexus.repository.TeamSessionRepository;
import com.shadownet.nexus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TeamSessionService {

    @Autowired
    private TeamSessionRepository teamSessionRepository;

    @Autowired
    private GameplayConsequenceService gameplayConsequenceService;

    @Autowired
    private UserRepository userRepository;

    public TeamSession createTeam(String userId, String missionId) {
        TeamSession session = new TeamSession();
        String id = "team-" + UUID.randomUUID().toString().substring(0, 8);
        String resolvedMissionId = missionId == null || missionId.isBlank() ? "mission_heist_001" : missionId;
        session.setId(id);
        session.setTeamId(id);
        session.setMissionId(resolvedMissionId);
        session.setLeaderId(userId);
        session.setStatus("waiting");
        session.setCreatedAt(Instant.now().toEpochMilli());
        session.setUpdatedAt(Instant.now().toEpochMilli());
        session.getMembers().add(userId);
        session.getReadyMap().put(userId, false);
        appendActivity(session, "team:create", userId,
                "Team operation lobby created for mission " + resolvedMissionId + ".");
        return teamSessionRepository.save(session);
    }

    public TeamSession getTeamForUser(String teamId, String userId) {
        TeamSession session = teamSessionRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));
        if (!session.getMembers().contains(userId)) {
            throw new SecurityException("User is not a member of this team session");
        }
        return session;
    }

    public TeamSessionViewDTO toViewDTO(TeamSession session) {
        return TeamSessionViewDTO.builder()
                .id(session.getTeamId())
                .teamId(session.getTeamId())
                .sessionId(session.getTeamId())
                .missionId(session.getMissionId())
                .phase(mapPhase(session.getStatus()))
                .evidenceCount(session.getEvidenceMap().values().stream().mapToInt(Integer::intValue).sum())
                .members(getEnrichedMembers(session))
                .activity(session.getActivityLog())
                .trust(Map.of(
                        "currentTrust", 50,
                        "state", "NEUTRAL",
                        "reason", "Mission cell trust changes when evidence and accusations resolve.",
                        "gameplayEffect", "Low trust raises accusation risk; high trust improves mission stability."))
                .evidence(Map.of(
                        "items", session.getEvidenceMap(),
                        "threshold", 3,
                        "whyItMatters", "Evidence unlocks accusation and changes mission consequences."))
                .accusation(Map.of(
                        "unlocked", "ACCUSATION_UNLOCKED".equals(session.getStatus())
                                || session.getEvidenceMap().values().stream().mapToInt(Integer::intValue).sum() >= 3,
                        "result", session.getAccusationResult() == null ? "pending" : session.getAccusationResult(),
                        "consequence",
                        "Correct accusations stabilize trust; wrong accusations fracture the mission cell."))
                .accusationUnlocked("ACCUSATION_UNLOCKED".equals(session.getStatus())
                        || session.getEvidenceMap().values().stream().mapToInt(Integer::intValue).sum() >= 3)
                .accusationResult(session.getAccusationResult())
                .build();
    }

    public List<MemberViewDTO> getEnrichedMembers(TeamSession session) {
        return session.getMembers().stream()
                .map(memberId -> {
                    User user = userRepository.findById(memberId).orElse(null);
                    String operatorCodename = user != null ? user.getSelectedOperator() : "Unknown";
                    return MemberViewDTO.builder()
                            .userId(memberId)
                            .username(user != null ? user.getUsername() : memberId)
                            .displayName(user != null ? user.getDisplayName() : memberId)
                            .operatorCodename(operatorCodename)
                            .operatorPortrait("https://api.dicebear.com/7.x/avataaars-neutral/svg?seed=" + memberId)
                            .role(session.getLeaderId() != null && session.getLeaderId().equals(memberId) ? "leader"
                                    : "member")
                            .ready(session.getReadyMap() != null
                                    && Boolean.TRUE.equals(session.getReadyMap().get(memberId)))
                            .connected(true) // enhance w/ WS presence later
                            .contributionSummary("Active participant")
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String mapPhase(String status) {
        return switch (status) {
            case "ACCUSATION_RESOLVED", "ACCUSATION_UNLOCKED" -> "accusation";
            case "active" -> "active";
            default -> "lobby";
        };
    }

    public TeamSession joinTeam(String teamId, String userId) {
        TeamSession session = teamSessionRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));
        if (!"waiting".equals(session.getStatus())) {
            throw new IllegalStateException("Team session has already started");
        }
        if (!session.getMembers().contains(userId) && session.getMembers().size() >= 4) {
            throw new IllegalStateException("Team session is full");
        }
        if (!session.getMembers().contains(userId)) {
            session.getMembers().add(userId);
            appendActivity(session, "team:join", userId, "Joined team lobby.");
        }
        session.getReadyMap().putIfAbsent(userId, false);
        session.setUpdatedAt(Instant.now().toEpochMilli());
        return teamSessionRepository.save(session);
    }

    public TeamSession toggleReady(String teamId, String userId, boolean ready) {
        TeamSession session = teamSessionRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));
        if (!session.getMembers().contains(userId)) {
            throw new SecurityException("User is not a member of this team session");
        }
        if (!"waiting".equals(session.getStatus())) {
            throw new IllegalStateException("Ready state can only change before the mission starts");
        }
        session.getReadyMap().put(userId, ready);
        appendActivity(session, ready ? "team:ready" : "team:not_ready", userId,
                ready ? "Marked ready." : "Cleared ready state.");
        session.setUpdatedAt(Instant.now().toEpochMilli());
        session.serializeEvidenceMap();
        return teamSessionRepository.save(session);
    }

    public TeamSession startTeam(String teamId, String userId) {
        TeamSession session = teamSessionRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));
        String leaderId = session.getLeaderId() == null || session.getLeaderId().isBlank()
                ? (session.getMembers().isEmpty() ? null : session.getMembers().get(0))
                : session.getLeaderId();
        if (leaderId == null || !leaderId.equals(userId)) {
            throw new SecurityException("Only the team leader can start this session");
        }
        boolean allReady = !session.getMembers().isEmpty()
                && session.getMembers().stream()
                        .allMatch(member -> Boolean.TRUE.equals(session.getReadyMap().get(member)));
        if (!allReady) {
            throw new IllegalStateException("All team members must be ready before start");
        }

        // Assign hidden traitor when mission starts (if there are enough players)
        if (session.getMembers().size() >= 2 && (session.getTraitorId() == null || session.getTraitorId().isBlank())) {
            // Get potential traitors (all members EXCEPT the leader)
            List<String> potentialTraitors = session.getMembers().stream()
                    .filter(m -> !m.equals(leaderId))
                    .collect(Collectors.toList());
            if (!potentialTraitors.isEmpty()) {
                // Select random traitor from non-leader members
                int traitorIndex = (int) (Math.random() * potentialTraitors.size());
                String hiddenTraitor = potentialTraitors.get(traitorIndex);
                session.setTraitorId(hiddenTraitor);
                appendActivity(session, "mission:traitor_assigned", "SYSTEM",
                        "WARNING: Potential infiltrator detected within team cell. Proceed with caution.");
            }
        }

        session.setStatus("active");
        session.setTimeStarted(Instant.now().toEpochMilli());
        appendActivity(session, "team:start", userId,
                "Mission started. " + session.getMembers().size() + " operators deployed.");
        session.setUpdatedAt(Instant.now().toEpochMilli());
        session.serializeEvidenceMap();
        return teamSessionRepository.save(session);
    }

    public TeamSession addEvidence(String teamId, String userId, String evidenceType) {
        return gameplayConsequenceService.applyTeamEvidence(teamId, userId, evidenceType);
    }

    public TeamSession accuse(String teamId, String userId, String accusedId) {
        return gameplayConsequenceService.applyTeamAccusation(teamId, userId, accusedId);
    }

    public void appendActivity(TeamSession session, String type, String userId, String summary) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", type);
        event.put("data", Map.of(
                "user", userId,
                "summary", summary == null ? type : summary));
        event.put("timestamp", Instant.now().toString());
        session.getActivityLog().add(event);
        if (session.getActivityLog().size() > 50) {
            session.setActivityLog(new java.util.ArrayList<>(session.getActivityLog()
                    .subList(session.getActivityLog().size() - 50, session.getActivityLog().size())));
        }
        session.serializeEvidenceMap();
    }
}
