package com.shadownet.nexus.service;

import com.shadownet.nexus.dto.DecisionRequestDTO;
import com.shadownet.nexus.dto.DecisionResponseDTO;
import com.shadownet.nexus.entity.*;
import com.shadownet.nexus.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoryServiceTest {

    @Mock private StoryChapterRepository chapterRepository;
    @Mock private StorySceneRepository sceneRepository;
    @Mock private StoryProgressRepository progressRepository;
    @Mock private UserRepository userRepository;
    @Mock private TrustService trustService;
    @Mock private UserStoryEvidenceRepository userStoryEvidenceRepository;
    @Mock private UserMissionStateRepository userMissionStateRepository;
    @Mock private MissionRepository missionRepository;
    @Mock private GameplayConsequenceService gameplayConsequenceService;

    @InjectMocks private StoryService storyService;

    @Test
    void makeDecision_PersistsProgressTrustEvidenceMissionAndOperatorInterpretation() {
        Fixture fixture = fixture();
        when(userRepository.findByUsername("agent")).thenReturn(fixture.user);
        when(progressRepository.findByUser(fixture.user)).thenReturn(Optional.of(fixture.progress));
        when(sceneRepository.findById(1L)).thenReturn(Optional.of(fixture.scene));
        when(sceneRepository.findById(2L)).thenReturn(Optional.of(fixture.nextScene));
        when(progressRepository.save(any(StoryProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gameplayConsequenceService.applyStoryDecisionConsequences(fixture.user, fixture.scene, fixture.choice))
                .thenReturn(new GameplayConsequenceService.StoryDecisionConsequence(
                        7,
                        7,
                        com.shadownet.nexus.dto.EvidenceDTO.builder()
                                .id(10L)
                                .evidenceCode("story-c1-s1-choice1")
                                .title("Scene 1 operational evidence")
                                .missionRelevanceTag("web")
                                .newlyDiscovered(true)
                                .build(),
                        List.of(com.shadownet.nexus.dto.MissionConsequenceDTO.builder()
                                .missionId("mission-web")
                                .state("RECOMMENDED")
                                .newlyChanged(true)
                                .build()),
                        com.shadownet.nexus.dto.OperatorInterpretationDTO.builder()
                                .operatorId("op_analyst")
                                .lens("Analyst lens")
                                .evidenceAngle("web")
                                .build()));

        DecisionResponseDTO response = storyService.makeDecision(
                DecisionRequestDTO.builder().sceneId(1L).choiceId(1L).build(),
                "agent");

        assertThat(response.getProgress().getCurrentSceneId()).isEqualTo(2L);
        assertThat(response.getTrustDelta()).isEqualTo(7);
        assertThat(response.getUpdatedTrust()).isEqualTo(7);
        assertThat(response.getEvidenceGained()).hasSize(1);
        assertThat(response.getEvidenceGained().get(0).getEvidenceCode()).isEqualTo("story-c1-s1-choice1");
        assertThat(response.getRecommendedMissionIds()).containsExactly("mission-web");
        assertThat(response.getOperatorInterpretation()).isNotNull();
        assertThat(response.getOperatorInterpretation().getLens()).contains("Analyst lens");
        assertThat(fixture.progress.getChoicesMade()).containsEntry(1L, 1L);
        verify(gameplayConsequenceService).applyStoryDecisionConsequences(fixture.user, fixture.scene, fixture.choice);
    }

    @Test
    void makeDecision_RejectsDecisionForNonCurrentScene() {
        Fixture fixture = fixture();
        fixture.progress.setCurrentSceneId(99L);
        when(userRepository.findByUsername("agent")).thenReturn(fixture.user);
        when(progressRepository.findByUser(fixture.user)).thenReturn(Optional.of(fixture.progress));
        when(sceneRepository.findById(1L)).thenReturn(Optional.of(fixture.scene));

        assertThatThrownBy(() -> storyService.makeDecision(
                DecisionRequestDTO.builder().sceneId(1L).choiceId(1L).build(),
                "agent"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("current story scene");

        verify(trustService, never()).updateTrustScore(anyString(), anyString(), anyInt());
        verify(userStoryEvidenceRepository, never()).save(any());
        verify(gameplayConsequenceService, never()).applyStoryDecisionConsequences(any(), any(), any());
    }

    @Test
    void makeDecision_DuplicateSameChoiceIsIdempotentAndDoesNotMutateTrustAgain() {
        Fixture fixture = fixture();
        fixture.progress.getChoicesMade().put(1L, 1L);
        fixture.progress.setCurrentSceneId(2L);
        when(userRepository.findByUsername("agent")).thenReturn(fixture.user);
        when(progressRepository.findByUser(fixture.user)).thenReturn(Optional.of(fixture.progress));
        when(sceneRepository.findById(1L)).thenReturn(Optional.of(fixture.scene));
        when(trustService.getTrustLevel(fixture.user, "STORY_Handler")).thenReturn(7);

        DecisionResponseDTO response = storyService.makeDecision(
                DecisionRequestDTO.builder().sceneId(1L).choiceId(1L).build(),
                "agent");

        assertThat(response.getTrustDelta()).isZero();
        assertThat(response.getUpdatedTrust()).isEqualTo(7);
        assertThat(response.getConsequenceFlags()).contains("duplicate_decision_ignored");
        assertThat(response.getEvidenceGained()).isEmpty();
        verify(trustService, never()).updateTrustScore(anyString(), anyString(), anyInt());
        verify(userStoryEvidenceRepository, never()).save(any());
        verify(userMissionStateRepository, never()).save(any());
        verify(gameplayConsequenceService, never()).applyStoryDecisionConsequences(any(), any(), any());
    }

    private Fixture fixture() {
        User user = new User();
        user.setId("user-1");
        user.setUsername("agent");
        user.setSelectedOperator("op_analyst");

        StoryChapter chapter = StoryChapter.builder()
                .id(1L)
                .chapterNumber(1)
                .title("Recruitment")
                .description("Briefing")
                .isLocked(false)
                .requiredTrustLevel(0)
                .build();

        StoryScene.SceneChoice choice = new StoryScene.SceneChoice(1L, "Correlate the firewall clue", 7, 2L);
        StoryScene scene = StoryScene.builder()
                .id(1L)
                .chapter(chapter)
                .sceneNumber(1)
                .content("Firewall trace recovered")
                .sceneType("CHOICE")
                .characterSpeaking("Handler")
                .operatorPovVariants("{}")
                .choices(List.of(choice, new StoryScene.SceneChoice(2L, "Ignore it", -5, 2L)))
                .nextSceneId(null)
                .build();

        StoryScene nextScene = StoryScene.builder()
                .id(2L)
                .chapter(chapter)
                .sceneNumber(2)
                .content("Next")
                .sceneType("NARRATIVE")
                .characterSpeaking("Handler")
                .operatorPovVariants("{}")
                .choices(List.of())
                .build();

        StoryProgress progress = StoryProgress.builder()
                .id(1L)
                .user(user)
                .currentChapterId(1L)
                .currentSceneId(1L)
                .completedChapters(new ArrayList<>())
                .choicesMade(new HashMap<>())
                .updatedAt(LocalDateTime.now())
                .build();

        Mission mission = new Mission();
        mission.setId("mission-web");
        mission.setTitle("Web Perimeter");
        mission.setMissionType("web");
        mission.setDifficulty("easy");
        mission.setStory("Follow the firewall clue.");
        mission.setMeta("{\"tag\":\"web\"}");

        return new Fixture(user, chapter, scene, nextScene, progress, mission, choice);
    }

    private record Fixture(
            User user,
            StoryChapter chapter,
            StoryScene scene,
            StoryScene nextScene,
            StoryProgress progress,
            Mission mission,
            StoryScene.SceneChoice choice) {
    }
}
