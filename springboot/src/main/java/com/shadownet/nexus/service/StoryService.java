package com.shadownet.nexus.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shadownet.nexus.dto.*;
import com.shadownet.nexus.entity.*;
import com.shadownet.nexus.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoryService {

        private final StoryChapterRepository chapterRepository;
        private final StorySceneRepository sceneRepository;
        private final StoryProgressRepository progressRepository;
        private final UserRepository userRepository;
        private final TrustService trustService;
        private final GameplayConsequenceService gameplayConsequenceService;
        private final UserStoryEvidenceRepository userStoryEvidenceRepository;
        private final ObjectMapper objectMapper = new ObjectMapper();

        public List<ChapterDTO> getChapters(String username) {
                User user = resolveUser(username);

                StoryProgress progress = progressRepository.findByUser(user).orElse(null);
                List<StoryChapter> chapters = chapterRepository.findAllByOrderByChapterNumberAsc();
                return chapters.stream().map(chapter -> convertToChapterDTO(chapter, progress, user))
                                .collect(Collectors.toList());
        }

        public ChapterDTO getChapter(Long id) {
                StoryChapter chapter = chapterRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Chapter not found"));
                return convertToChapterDTO(chapter, null, null);
        }

        public SceneDTO getFirstScene(Long chapterId) {
                StoryScene scene = sceneRepository.findFirstByChapter_IdOrderBySceneNumberAsc(chapterId)
                                .orElseThrow(() -> new RuntimeException("No scenes found for chapter"));
                return convertToSceneDTO(scene);
        }

        public SceneDTO getScene(Long id) {
                StoryScene scene = sceneRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Scene not found"));
                return convertToSceneDTO(scene);
        }

        public StoryProgressDTO getProgress(String username) {
                User user = resolveUser(username);

                StoryProgress progress = progressRepository.findByUser(user)
                                .orElseGet(() -> createInitialProgress(user));
                return convertToProgressDTO(progress);
        }

        public ChapterDebriefDTO getChapterDebrief(Long chapterId, String username) {
                User user = resolveUser(username);
                StoryChapter chapter = chapterRepository.findById(chapterId)
                                .orElseThrow(() -> new RuntimeException("Chapter not found"));
                StoryProgress progress = progressRepository.findByUser(user)
                                .orElseGet(() -> createInitialProgress(user));
                List<EvidenceDTO> found = userStoryEvidenceRepository.findByUserOrderByDiscoveredAtDesc(user).stream()
                                .filter(evidence -> chapterId.equals(evidence.getSourceChapterId()))
                                .map(evidence -> EvidenceDTO.builder()
                                                .id(evidence.getId())
                                                .evidenceCode(evidence.getEvidenceCode())
                                                .title(evidence.getTitle())
                                                .summary(evidence.getSummary())
                                                .sourceChapterId(evidence.getSourceChapterId())
                                                .sourceSceneId(evidence.getSourceSceneId())
                                                .sourceChoiceId(evidence.getSourceChoiceId())
                                                .operatorInterpretation(evidence.getOperatorInterpretation())
                                                .missionRelevanceTag(evidence.getMissionRelevanceTag())
                                                .discoveredAt(evidence.getDiscoveredAt() == null ? null
                                                                : evidence.getDiscoveredAt().toString())
                                                .newlyDiscovered(false)
                                                .build())
                                .toList();

                Set<String> foundCodes = found.stream().map(EvidenceDTO::getEvidenceCode).collect(Collectors.toSet());
                List<String> missed = sceneRepository.findByChapterIdOrderBySceneNumberAsc(chapterId).stream()
                                .flatMap(scene -> scene.getChoices().stream()
                                                .map(choice -> String.format("story-c%s-s%s-choice%s",
                                                                chapter.getChapterNumber(), scene.getSceneNumber(),
                                                                choice.getId())))
                                .filter(code -> !foundCodes.contains(code))
                                .toList();
                Integer trust = trustService.getTrustLevel(user, "STORY_GLOBAL");
                String conclusion = progress.getCompletedChapters().contains(chapterId)
                                ? "Chapter completed with " + found.size() + " evidence item(s) found."
                                : "Chapter in progress with " + found.size() + " evidence item(s) found.";

                return ChapterDebriefDTO.builder()
                                .chapterId(chapterId)
                                .playerConclusion(conclusion)
                                .evidenceFound(found)
                                .evidenceMissed(missed)
                                .trustOutcome(trust)
                                .nextOperationalRisk(missed.isEmpty()
                                                ? "Evidence coverage is complete for this chapter."
                                                : "Some evidence remains undiscovered; later conclusions may be incomplete.")
                                .build();
        }

        @Transactional
        public StoryProgressDTO saveProgressSnapshot(Map<String, Object> payload, String username) {
                User user = resolveUser(username);

                StoryProgress progress = progressRepository.findByUser(user)
                                .orElseGet(() -> createInitialProgress(user));
                Object rawProgress = payload.getOrDefault("storyProgress", payload);

                if (rawProgress != null) {
                        try {
                                user.setStoryProgress(objectMapper.writeValueAsString(rawProgress));
                                user.setUpdatedAt(System.currentTimeMillis());
                                userRepository.save(user);
                        } catch (Exception ignored) {
                        }
                }

                Object currentChapterId = payload.get("currentChapterId");
                Object currentSceneId = payload.get("currentSceneId");
                if (currentChapterId instanceof Number number) {
                        progress.setCurrentChapterId(number.longValue());
                }
                if (currentSceneId instanceof Number number) {
                        progress.setCurrentSceneId(number.longValue());
                }

                Object completedChapters = payload.get("completedChapters");
                if (completedChapters instanceof List<?> list) {
                        progress.setCompletedChapters(list.stream()
                                        .filter(Number.class::isInstance)
                                        .map(Number.class::cast)
                                        .map(Number::longValue)
                                        .collect(Collectors.toList()));
                }

                progress.setUpdatedAt(LocalDateTime.now());
                return convertToProgressDTO(progressRepository.save(progress));
        }

        @Transactional
        public DecisionResponseDTO makeDecision(DecisionRequestDTO request, String username) {
                User user = resolveUser(username);

                StoryProgress progress = progressRepository.findByUser(user)
                                .orElseGet(() -> createInitialProgress(user));
                StoryScene currentScene = sceneRepository.findById(request.getSceneId())
                                .orElseThrow(() -> new RuntimeException("Scene not found"));

                Map<Long, Long> choicesMade = ensureChoicesMade(progress);
                Long previousChoiceId = choicesMade.get(currentScene.getId());
                boolean isDuplicate = previousChoiceId != null;

                var chosenChoice = currentScene.getChoices().stream()
                                .filter(c -> c.getId().equals(request.getChoiceId()))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Invalid choice"));

                Long nextSceneId = chosenChoice.getNextSceneId();
                Long nextChapterId = null;
                List<String> flags = new ArrayList<>();

                if (isDuplicate) {
                        if (!previousChoiceId.equals(request.getChoiceId())) {
                                throw new IllegalStateException(
                                                "Story decision already locked for this scene. Reload story state before continuing.");
                        }
                        flags.add("duplicate_decision_ignored");
                        // Safe replay - compute next from the already-recorded choice.
                        if (nextSceneId == null) {
                                StoryChapter nextChapter = chapterRepository
                                                .findFirstByChapterNumberGreaterThanOrderByChapterNumberAsc(
                                                                currentScene.getChapter().getChapterNumber())
                                                .orElse(null);
                                if (nextChapter != null) {
                                        StoryScene firstScene = sceneRepository
                                                        .findFirstByChapter_IdOrderBySceneNumberAsc(nextChapter.getId())
                                                        .orElse(null);
                                        nextSceneId = firstScene != null ? firstScene.getId() : null;
                                }
                        }
                        // No trust/evidence/mission change for duplicate
                        return buildDecisionResponse(user, progress, currentScene, chosenChoice, nextSceneId, null,
                                        0, trustService.getTrustLevel(user, buildTrustTarget(currentScene)),
                                        Collections.emptyList(), Collections.emptyList(), flags, true, null);
                }

                // Fresh decision - full application
                if (progress.getCurrentSceneId() != null
                                && !progress.getCurrentSceneId().equals(currentScene.getId())) {
                        throw new IllegalStateException(
                                        "Decision is not for the current story scene. Reload story state before continuing.");
                }

                choicesMade.put(currentScene.getId(), request.getChoiceId());

                if (nextSceneId == null) {
                        StoryChapter nextChapter = chapterRepository
                                        .findFirstByChapterNumberGreaterThanOrderByChapterNumberAsc(
                                                        currentScene.getChapter().getChapterNumber())
                                        .orElse(null);

                        if (nextChapter != null) {
                                nextChapterId = nextChapter.getId();
                                StoryScene firstScene = sceneRepository
                                                .findFirstByChapter_IdOrderBySceneNumberAsc(nextChapterId)
                                                .orElse(null);
                                nextSceneId = firstScene != null ? firstScene.getId() : null;

                                if (!progress.getCompletedChapters().contains(currentScene.getChapter().getId())) {
                                        progress.getCompletedChapters().add(currentScene.getChapter().getId());
                                }
                        }
                }

                if (nextSceneId != null) {
                        progress.setCurrentSceneId(nextSceneId);
                        progress.setCurrentChapterId(sceneRepository.findById(nextSceneId).get().getChapter().getId());
                }

                progress.setUpdatedAt(LocalDateTime.now());
                progressRepository.save(progress);

                GameplayConsequenceService.StoryDecisionConsequence consequence = gameplayConsequenceService
                                .applyStoryDecisionConsequences(user, currentScene, chosenChoice);

                return buildDecisionResponse(user, progress, currentScene, chosenChoice, nextSceneId, nextChapterId,
                                consequence.trustDelta(), consequence.updatedTrust(),
                                consequence.evidence() == null ? Collections.emptyList()
                                                : List.of(consequence.evidence()),
                                consequence.missionChanges(),
                                flags, false, consequence.operatorInterpretation());
        }

        private DecisionResponseDTO buildDecisionResponse(
                        User user,
                        StoryProgress progress,
                        StoryScene scene,
                        StoryScene.SceneChoice choice,
                        Long nextSceneId,
                        Long nextChapterId,
                        int trustDelta,
                        int updatedTrust,
                        List<EvidenceDTO> evidence,
                        List<MissionConsequenceDTO> missionChanges,
                        List<String> flags,
                        boolean duplicate,
                        OperatorInterpretationDTO operatorInterpretation) {
                StoryConsequenceSummaryDTO summary = buildSummary(scene, choice, trustDelta, evidence, missionChanges,
                                operatorInterpretation, duplicate);
                List<String> unlockedMissionIds = missionChanges.stream()
                                .filter(change -> "UNLOCKED".equals(change.getState()))
                                .map(MissionConsequenceDTO::getMissionId)
                                .toList();
                List<String> recommendedMissionIds = missionChanges.stream()
                                .filter(change -> "RECOMMENDED".equals(change.getState()))
                                .map(MissionConsequenceDTO::getMissionId)
                                .toList();

                return DecisionResponseDTO.builder()
                                .progress(convertToProgressDTO(progress))
                                .nextSceneId(nextSceneId)
                                .nextChapterId(nextChapterId)
                                .trustImpact(trustDelta)
                                .trustDelta(trustDelta)
                                .updatedTrust(updatedTrust)
                                .targetEntity(buildTrustTarget(scene))
                                .evidenceGained(evidence)
                                .consequenceFlags(flags)
                                .unlockedMissionIds(unlockedMissionIds)
                                .recommendedMissionIds(recommendedMissionIds)
                                .missionChanges(missionChanges)
                                .consequenceSummary(summary)
                                .operatorInterpretation(operatorInterpretation)
                                .build();
        }

        private Map<Long, Long> ensureChoicesMade(StoryProgress progress) {
                if (progress.getChoicesMade() == null) {
                        progress.setChoicesMade(new HashMap<>());
                }
                return progress.getChoicesMade();
        }

        private String buildTrustTarget(StoryScene scene) {
                String speaker = Optional.ofNullable(scene.getCharacterSpeaking()).orElse("GLOBAL");
                return "STORY_" + speaker;
        }

        private StoryConsequenceSummaryDTO buildSummary(
                        StoryScene scene,
                        StoryScene.SceneChoice choice,
                        int trustDelta,
                        List<EvidenceDTO> evidence,
                        List<MissionConsequenceDTO> missionChanges,
                        OperatorInterpretationDTO operatorInterpretation,
                        boolean duplicate) {
                if (duplicate) {
                        return StoryConsequenceSummaryDTO.builder()
                                        .summary(
                                                        "Decision was already recorded; no additional trust, evidence, or mission change was applied.")
                                        .playerConclusion("Repeated decision ignored")
                                        .nextOperationalRisk("None added")
                                        .build();
                }

                String evidenceText = evidence.isEmpty() ? "no new evidence" : evidence.get(0).getEvidenceCode();
                String missionText = missionChanges.isEmpty() ? "no mission state change"
                                : missionChanges.get(0).getState().toLowerCase(Locale.ROOT) + " "
                                                + missionChanges.get(0).getMissionId();
                String operatorText = operatorInterpretation == null ? "" : " " + operatorInterpretation.getLens();
                return StoryConsequenceSummaryDTO.builder()
                                .summary("Choice " + choice.getId() + " in chapter "
                                                + scene.getChapter().getChapterNumber()
                                                + " changed trust by " + trustDelta + ", recorded " + evidenceText
                                                + ", and produced "
                                                + missionText + "." + operatorText)
                                .playerConclusion(choice.getText())
                                .nextOperationalRisk(trustDelta < 0
                                                ? "Trust degradation may unlock higher-risk follow-up operations."
                                                : "Positive trust improves recommended follow-up operations.")
                                .build();
        }

        @Transactional
        public void resetProgress(String username) {
                User user = resolveUser(username);

                progressRepository.findByUser(user).ifPresent(progressRepository::delete);
                createInitialProgress(user);
        }

        private User resolveUser(String principal) {
                return userRepository.findById(principal)
                                .orElseGet(() -> {
                                        User user = userRepository.findByUsername(principal);
                                        if (user == null) {
                                                throw new RuntimeException("User not found");
                                        }
                                        return user;
                                });
        }

        private StoryProgress createInitialProgress(User user) {
                StoryChapter firstChapter = chapterRepository.findAllByOrderByChapterNumberAsc().get(0);
                StoryScene firstScene = sceneRepository.findFirstByChapter_IdOrderBySceneNumberAsc(firstChapter.getId())
                                .orElseThrow(() -> new RuntimeException("No scenes found"));

                StoryProgress progress = StoryProgress.builder()
                                .user(user)
                                .currentChapterId(firstChapter.getId())
                                .currentSceneId(firstScene.getId())
                                .completedChapters(new ArrayList<>())
                                .choicesMade(new HashMap<>())
                                .updatedAt(LocalDateTime.now())
                                .build();

                return progressRepository.save(progress);
        }

        private ChapterDTO convertToChapterDTO(StoryChapter chapter, StoryProgress progress, User user) {
                boolean isLocked = chapter.isLocked();
                return ChapterDTO.builder()
                                .id(chapter.getId())
                                .chapterNumber(chapter.getChapterNumber())
                                .title(chapter.getTitle())
                                .description(chapter.getDescription())
                                .isLocked(isLocked)
                                .requiredTrustLevel(chapter.getRequiredTrustLevel())
                                .build();
        }

        private SceneDTO convertToSceneDTO(StoryScene scene) {
                return SceneDTO.builder()
                                .id(scene.getId())
                                .chapterId(scene.getChapter().getId())
                                .sceneNumber(scene.getSceneNumber())
                                .sceneType(scene.getSceneType())
                                .content(scene.getContent())
                                .characterSpeaking(scene.getCharacterSpeaking())
                                .operatorPovVariants(scene.getOperatorPovVariants())
                                .choices(scene.getChoices())
                                .nextSceneId(scene.getNextSceneId())
                                .build();
        }

        private StoryProgressDTO convertToProgressDTO(StoryProgress progress) {
                int totalChapters = (int) chapterRepository.count();
                int completedChapters = progress.getCompletedChapters().size();
                int completionPercentage = totalChapters > 0 ? (completedChapters * 100) / totalChapters : 0;

                return StoryProgressDTO.builder()
                                .userId(progress.getUser().getId().toString())
                                .currentChapterId(progress.getCurrentChapterId())
                                .currentSceneId(progress.getCurrentSceneId())
                                .completedChapters(progress.getCompletedChapters())
                                .choicesMade(progress.getChoicesMade())
                                .completionPercentage(completionPercentage)
                                .build();
        }
}
