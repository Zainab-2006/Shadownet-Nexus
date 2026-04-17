package com.shadownet.nexus.service;

import com.shadownet.nexus.dto.MissionConsequenceDTO;
import com.shadownet.nexus.dto.OperatorConsequenceRequestDTO;
import com.shadownet.nexus.dto.OperatorConsequenceResponseDTO;
import com.shadownet.nexus.dto.StoryConsequenceSummaryDTO;
import com.shadownet.nexus.entity.*;
import com.shadownet.nexus.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GameplayConsequenceService {

    private final UserRepository userRepository;
    private final OperatorRepository operatorRepository;
    private final MissionRepository missionRepository;
    private final UserMissionStateRepository userMissionStateRepository;
    private final UserStoryEvidenceRepository userStoryEvidenceRepository;
    private final TeamSessionRepository teamSessionRepository;
    private final TrustService trustService;

    public record StoryDecisionConsequence(
            int trustDelta,
            int updatedTrust,
            com.shadownet.nexus.dto.EvidenceDTO evidence,
            List<MissionConsequenceDTO> missionChanges,
            com.shadownet.nexus.dto.OperatorInterpretationDTO operatorInterpretation) {
    }

    @Transactional
    public StoryDecisionConsequence applyStoryDecisionConsequences(
            User user,
            StoryScene scene,
            StoryScene.SceneChoice choice) {
        String targetEntity = "STORY_" + (scene.getCharacterSpeaking() == null ? "GLOBAL" : scene.getCharacterSpeaking());
        int trustDelta = choice.getTrustImpact() == null ? 0 : choice.getTrustImpact();
        int updatedTrust = trustService.getTrustLevel(user, targetEntity);
        if (trustDelta != 0) {
            updatedTrust = trustService.updateTrustScore(user.getId(), targetEntity, trustDelta).getTrustScore();
        }

        com.shadownet.nexus.dto.EvidenceDTO evidence = awardStoryEvidence(user, scene, choice);
        MissionConsequenceDTO missionChange = applyStoryMissionConsequence(user, scene, choice, evidence);
        List<MissionConsequenceDTO> missionChanges = missionChange == null ? List.of() : List.of(missionChange);
        com.shadownet.nexus.dto.OperatorInterpretationDTO operatorInterpretation =
                buildStoryOperatorInterpretation(user.getSelectedOperator(), scene, choice, evidence);

        return new StoryDecisionConsequence(trustDelta, updatedTrust, evidence, missionChanges, operatorInterpretation);
    }

    @Transactional
    public OperatorConsequenceResponseDTO applyOperatorConsequence(
            String username,
            String operatorId,
            OperatorConsequenceRequestDTO request) {
        User user = requireUser(username);
        Operator operator = operatorRepository.findById(operatorId)
                .orElseThrow(() -> new IllegalArgumentException("Operator not found"));

        String missionId = request.getMissionId();
        Mission mission = null;
        if (missionId != null && !missionId.isBlank()) {
            mission = missionRepository.findById(missionId)
                    .orElseThrow(() -> new IllegalArgumentException("Mission not found"));
        }

        String outcome = normalizeOutcome(request.getOutcome());
        String choiceId = request.getChoiceId() == null || request.getChoiceId().isBlank()
                ? "operator-choice"
                : request.getChoiceId();
        String targetEntity = "OPERATOR_" + operator.getId();
        int trustDelta = calculateOperatorTrustDelta(operator, mission, outcome, request.getAction());
        TrustEntity trust = trustService.updateTrustScore(user.getId(), targetEntity, trustDelta);

        List<MissionConsequenceDTO> missionChanges = new ArrayList<>();
        if (mission != null) {
            String missionOutcome = "success".equals(outcome) ? "recommend" : outcome;
            missionChanges.add(applyMissionState(user, mission.getId(), missionOutcome, choiceId,
                    "Operator " + operator.getId() + " consequence from " + outcome + " action."));
        }

        return OperatorConsequenceResponseDTO.builder()
                .operatorId(operator.getId())
                .missionId(mission == null ? null : mission.getId())
                .choiceId(choiceId)
                .outcome(outcome)
                .trustDelta(trustDelta)
                .updatedTrust(trust.getTrustScore())
                .targetEntity(targetEntity)
                .missionChanges(missionChanges)
                .consequenceFlags(List.of("backend_authored_operator_consequence"))
                .consequenceSummary(StoryConsequenceSummaryDTO.builder()
                        .summary("Operator consequence applied server-side for " + operator.getName() + ".")
                        .playerConclusion(outcome)
                        .nextOperationalRisk(trustDelta < 0
                                ? "Trust degraded after a risky operator action."
                                : "Trust improved after a controlled operator action.")
                        .build())
                .build();
    }

    @Transactional
    public MissionConsequenceDTO applyMissionAction(String username, String missionId, String action) {
        User user = requireUser(username);
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found"));
        String normalizedAction = action == null ? "RECOMMEND" : action.trim().toUpperCase(Locale.ROOT);
        String outcome = switch (normalizedAction) {
            case "START" -> "active";
            case "UNLOCK" -> "unlock";
            case "COMPLETE" -> "complete";
            default -> "recommend";
        };
        return applyMissionState(user, mission.getId(), outcome, normalizedAction,
                "Mission action " + normalizedAction + " applied by backend consequence service.");
    }

    @Transactional
    public TeamSession applyTeamEvidence(String teamId, String username, String evidenceType) {
        User user = requireUser(username);
        TeamSession session = requireTeamMember(teamId, user.getId());
        if (!"active".equals(session.getStatus()) && !"ACCUSATION_UNLOCKED".equals(session.getStatus())) {
            throw new IllegalStateException("Evidence can only be added during an active team session");
        }
        if ("ACCUSATION_UNLOCKED".equals(session.getStatus())) {
            throw new IllegalStateException("Accusation is already unlocked for this team session");
        }
        String type = evidenceType == null || evidenceType.isBlank() ? "clue" : evidenceType.trim().toLowerCase(Locale.ROOT);
        session.getEvidenceMap().merge(type, 1, Integer::sum);
        int totalEvidence = session.getEvidenceMap().values().stream().mapToInt(Integer::intValue).sum();
        if (totalEvidence >= 3) {
            session.setStatus("ACCUSATION_UNLOCKED");
        } else if (!"ACCUSATION_UNLOCKED".equals(session.getStatus())) {
            session.setStatus("active");
        }
        trustService.updateTrustScore(user.getId(), "TEAM_" + teamId, 1);
        appendTeamActivity(session, "team:evidence", user.getId(), "Recorded " + type + " evidence. Total evidence: " + totalEvidence + ".");
        session.serializeEvidenceMap();
        return teamSessionRepository.save(session);
    }

    @Transactional
    public TeamSession applyTeamAccusation(String teamId, String username, String accusedId) {
        User user = requireUser(username);
        TeamSession session = requireTeamMember(teamId, user.getId());
        if (!"ACCUSATION_UNLOCKED".equals(session.getStatus())) {
            throw new IllegalStateException("Accusation is not unlocked for this team session");
        }
        String expectedTraitor = session.getTraitorId();
        boolean hasRevealedTarget = expectedTraitor != null && !expectedTraitor.isBlank();
        boolean correct = hasRevealedTarget && accusedId != null && accusedId.equalsIgnoreCase(expectedTraitor);
        session.setAccusationResult(hasRevealedTarget ? (correct ? "CORRECT" : "INCORRECT") : "UNRESOLVED_HIDDEN_HAND");
        session.setStatus("ACCUSATION_RESOLVED");
        trustService.updateTrustScore(user.getId(), "TEAM_" + teamId, correct ? 5 : -8);
        appendTeamActivity(session, "team:accuse", user.getId(), "Accused " + accusedId + ": " + session.getAccusationResult() + ".");
        session.serializeEvidenceMap();
        return teamSessionRepository.save(session);
    }

    public List<com.shadownet.nexus.dto.UserMissionStateDTO> getMissionStates(String username) {
        User user = requireUser(username);
        return userMissionStateRepository.findByUserOrderByUpdatedAtDesc(user).stream()
                .map(state -> com.shadownet.nexus.dto.UserMissionStateDTO.builder()
                        .missionId(state.getMissionId())
                        .state(state.getState())
                        .reason(state.getReason())
                        .sourceChapterId(state.getSourceChapterId())
                        .sourceSceneId(state.getSourceSceneId())
                        .sourceChoiceId(state.getSourceChoiceId())
                        .updatedAt(state.getUpdatedAt() == null ? null : state.getUpdatedAt().toString())
                        .build())
                .toList();
    }

    private User requireUser(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            return user;
        }
        return userRepository.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private TeamSession requireTeamMember(String teamId, String userId) {
        TeamSession session = teamSessionRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team session not found"));
        if (!session.getMembers().contains(userId)) {
            throw new SecurityException("User is not a member of this team session");
        }
        return session;
    }

    private void appendTeamActivity(TeamSession session, String type, String userId, String summary) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", type);
        event.put("data", Map.of("user", userId, "summary", summary));
        event.put("timestamp", LocalDateTime.now().toString());
        session.getActivityLog().add(event);
        if (session.getActivityLog().size() > 50) {
            session.setActivityLog(new ArrayList<>(session.getActivityLog().subList(session.getActivityLog().size() - 50, session.getActivityLog().size())));
        }
        session.serializeEvidenceMap();
    }

    private String normalizeOutcome(String outcome) {
        if (outcome == null || outcome.isBlank()) {
            return "neutral";
        }
        return outcome.trim().toLowerCase(Locale.ROOT);
    }

    private int calculateOperatorTrustDelta(Operator operator, Mission mission, String outcome, String action) {
        int base = switch (outcome) {
            case "success", "support", "collaborate" -> 6;
            case "failure", "risk", "hostile" -> -6;
            default -> 1;
        };
        String role = operator.getRole() == null ? "" : operator.getRole().toLowerCase(Locale.ROOT);
        String missionType = mission == null || mission.getMissionType() == null
                ? ""
                : mission.getMissionType().toLowerCase(Locale.ROOT);
        if (!missionType.isBlank() && role.contains(missionType)) {
            base += base >= 0 ? 2 : -2;
        }
        if (action != null && action.toLowerCase(Locale.ROOT).contains("reckless")) {
            base -= 3;
        }
        return Math.max(-10, Math.min(10, base));
    }

    private com.shadownet.nexus.dto.EvidenceDTO awardStoryEvidence(User user, StoryScene scene, StoryScene.SceneChoice choice) {
        String evidenceCode = String.format("story-c%s-s%s-choice%s",
                scene.getChapter().getChapterNumber(), scene.getSceneNumber(), choice.getId());
        var existing = userStoryEvidenceRepository.findByUserAndEvidenceCode(user, evidenceCode);
        if (existing.isPresent()) {
            return toEvidenceDTO(existing.get(), false);
        }

        String missionTag = inferMissionTag(scene, choice);
        UserStoryEvidence evidence = UserStoryEvidence.builder()
                .user(user)
                .evidenceCode(evidenceCode)
                .title("Scene " + scene.getSceneNumber() + " operational evidence")
                .summary("Choice '" + choice.getText() + "' established a story consequence in chapter "
                        + scene.getChapter().getChapterNumber() + ".")
                .sourceChapterId(scene.getChapter().getId())
                .sourceSceneId(scene.getId())
                .sourceChoiceId(choice.getId())
                .operatorInterpretation(buildEvidenceInterpretation(user.getSelectedOperator(), missionTag))
                .missionRelevanceTag(missionTag)
                .discoveredAt(LocalDateTime.now())
                .build();
        return toEvidenceDTO(userStoryEvidenceRepository.save(evidence), true);
    }

    private MissionConsequenceDTO applyStoryMissionConsequence(
            User user,
            StoryScene scene,
            StoryScene.SceneChoice choice,
            com.shadownet.nexus.dto.EvidenceDTO evidence) {
        List<Mission> missions = missionRepository.findAll();
        if (missions.isEmpty()) {
            return null;
        }

        String tag = evidence == null ? inferMissionTag(scene, choice) : evidence.getMissionRelevanceTag();
        String missionId = missions.stream()
                .filter(mission -> containsIgnoreCase(mission.getMissionType(), tag)
                        || containsIgnoreCase(mission.getTitle(), tag)
                        || containsIgnoreCase(mission.getMeta(), tag))
                .map(Mission::getId)
                .findFirst()
                .orElse(missions.get(Math.floorMod(scene.getChapter().getChapterNumber() + choice.getId().intValue(), missions.size())).getId());
        String outcome = choice.getTrustImpact() != null && choice.getTrustImpact() >= 0 ? "recommend" : "unlock";
        return applyMissionState(user, missionId, outcome, String.valueOf(choice.getId()),
                "Story chapter " + scene.getChapter().getChapterNumber()
                        + " decision " + choice.getId() + " produced " + missionId + ".");
    }

    private com.shadownet.nexus.dto.OperatorInterpretationDTO buildStoryOperatorInterpretation(
            String operatorId,
            StoryScene scene,
            StoryScene.SceneChoice choice,
            com.shadownet.nexus.dto.EvidenceDTO evidence) {
        if (operatorId == null || operatorId.isBlank()) {
            return null;
        }
        String missionTag = evidence == null ? inferMissionTag(scene, choice) : evidence.getMissionRelevanceTag();
        return com.shadownet.nexus.dto.OperatorInterpretationDTO.builder()
                .operatorId(operatorId)
                .lens(buildEvidenceInterpretation(operatorId, missionTag))
                .evidenceAngle(missionTag)
                .missionEmphasis("Use " + missionTag + " context when selecting the next operation.")
                .build();
    }

    private String inferMissionTag(StoryScene scene, StoryScene.SceneChoice choice) {
        String combined = (scene.getContent() + " " + choice.getText()).toLowerCase(Locale.ROOT);
        if (combined.contains("firewall") || combined.contains("breach")) {
            return "web";
        }
        if (combined.contains("accuse") || combined.contains("sable")) {
            return "forensics";
        }
        if (combined.contains("cipher") || combined.contains("code")) {
            return "crypto";
        }
        return "story";
    }

    private String buildEvidenceInterpretation(String operatorId, String missionTag) {
        String operator = operatorId == null ? "unassigned" : operatorId.toLowerCase(Locale.ROOT);
        if (operator.contains("analyst")) {
            return "Analyst lens: preserve sequence, motive, and confidence level before acting on " + missionTag + " evidence.";
        }
        if (operator.contains("field")) {
            return "Field lens: treat the consequence as operational risk and prioritize containment.";
        }
        if (operator.contains("hacker")) {
            return "Hacker lens: validate the technical artifact before trusting the narrative signal.";
        }
        return "Operator lens: consequence recorded for later mission correlation.";
    }

    private boolean containsIgnoreCase(String value, String needle) {
        return value != null && needle != null && value.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }

    private com.shadownet.nexus.dto.EvidenceDTO toEvidenceDTO(UserStoryEvidence evidence, boolean newlyDiscovered) {
        return com.shadownet.nexus.dto.EvidenceDTO.builder()
                .id(evidence.getId())
                .evidenceCode(evidence.getEvidenceCode())
                .title(evidence.getTitle())
                .summary(evidence.getSummary())
                .sourceChapterId(evidence.getSourceChapterId())
                .sourceSceneId(evidence.getSourceSceneId())
                .sourceChoiceId(evidence.getSourceChoiceId())
                .operatorInterpretation(evidence.getOperatorInterpretation())
                .missionRelevanceTag(evidence.getMissionRelevanceTag())
                .discoveredAt(evidence.getDiscoveredAt() == null ? null : evidence.getDiscoveredAt().toString())
                .newlyDiscovered(newlyDiscovered)
                .build();
    }

    private MissionConsequenceDTO applyMissionState(User user, String missionId, String outcome, String choiceId, String reason) {
        String state = switch (outcome) {
            case "recommend", "support", "collaborate" -> "RECOMMENDED";
            case "unlock", "risk", "failure" -> "UNLOCKED";
            case "active" -> "ACTIVE";
            case "success", "complete" -> "COMPLETED";
            default -> "RECOMMENDED";
        };

        UserMissionState stateEntity = userMissionStateRepository.findByUserAndMissionId(user, missionId)
                .orElseGet(() -> UserMissionState.builder()
                        .user(user)
                        .missionId(missionId)
                        .build());
        boolean changed = stateEntity.getState() == null || !stateEntity.getState().equals(state);
        stateEntity.setState(state);
        stateEntity.setSourceChoiceId(hashChoice(choiceId));
        stateEntity.setReason(reason);
        stateEntity.setUpdatedAt(LocalDateTime.now());
        userMissionStateRepository.save(stateEntity);

        return MissionConsequenceDTO.builder()
                .missionId(missionId)
                .state(state)
                .reason(reason)
                .newlyChanged(changed)
                .build();
    }

    private Long hashChoice(String choiceId) {
        if (choiceId == null) {
            return null;
        }
        return (long) Math.abs(choiceId.hashCode());
    }
}
