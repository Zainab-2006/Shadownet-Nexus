package com.shadownet.nexus.service;

import com.shadownet.nexus.dto.OperatorConsequenceRequestDTO;
import com.shadownet.nexus.entity.Mission;
import com.shadownet.nexus.entity.Operator;
import com.shadownet.nexus.entity.TrustEntity;
import com.shadownet.nexus.entity.User;
import com.shadownet.nexus.entity.UserMissionState;
import com.shadownet.nexus.entity.TeamSession;
import com.shadownet.nexus.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameplayConsequenceServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private OperatorRepository operatorRepository;
    @Mock private MissionRepository missionRepository;
    @Mock private UserMissionStateRepository userMissionStateRepository;
    @Mock private UserStoryEvidenceRepository userStoryEvidenceRepository;
    @Mock private TeamSessionRepository teamSessionRepository;
    @Mock private TrustService trustService;

    @InjectMocks private GameplayConsequenceService gameplayConsequenceService;

    @Test
    void operatorConsequence_ComputesTrustServerSideAndPersistsMissionState() {
        User user = user();
        Operator operator = operator();
        Mission mission = mission();
        TrustEntity trust = new TrustEntity();
        trust.setTrustScore(6);

        when(userRepository.findByUsername("agent")).thenReturn(user);
        when(operatorRepository.findById("op_analyst")).thenReturn(Optional.of(operator));
        when(missionRepository.findById("mission-web")).thenReturn(Optional.of(mission));
        when(trustService.updateTrustScore("user-1", "OPERATOR_op_analyst", 6)).thenReturn(trust);
        when(userMissionStateRepository.findByUserAndMissionId(user, "mission-web")).thenReturn(Optional.empty());
        when(userMissionStateRepository.save(any(UserMissionState.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = gameplayConsequenceService.applyOperatorConsequence(
                "agent",
                "op_analyst",
                OperatorConsequenceRequestDTO.builder()
                        .missionId("mission-web")
                        .choiceId("choice-client-tried")
                        .outcome("success")
                        .action("ignored frontend delta")
                        .build());

        assertThat(response.getTrustDelta()).isEqualTo(6);
        assertThat(response.getUpdatedTrust()).isEqualTo(6);
        assertThat(response.getTargetEntity()).isEqualTo("OPERATOR_op_analyst");
        assertThat(response.getMissionChanges()).hasSize(1);
        assertThat(response.getMissionChanges().get(0).getState()).isEqualTo("RECOMMENDED");
        verify(trustService).updateTrustScore("user-1", "OPERATOR_op_analyst", 6);
        verify(userMissionStateRepository).save(any(UserMissionState.class));
    }

    @Test
    void missionAction_ComputesBackendStateWithoutClientResultPayload() {
        User user = user();
        Mission mission = mission();
        when(userRepository.findByUsername("agent")).thenReturn(user);
        when(missionRepository.findById("mission-web")).thenReturn(Optional.of(mission));
        when(userMissionStateRepository.findByUserAndMissionId(user, "mission-web")).thenReturn(Optional.empty());
        when(userMissionStateRepository.save(any(UserMissionState.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = gameplayConsequenceService.applyMissionAction("agent", "mission-web", "START");

        assertThat(response.getMissionId()).isEqualTo("mission-web");
        assertThat(response.getState()).isEqualTo("ACTIVE");
        verify(userMissionStateRepository).save(any(UserMissionState.class));
    }

    @Test
    void teamEvidence_RequiresMembershipAndUnlocksAccusationAtThreshold() {
        User user = user();
        TeamSession session = new TeamSession();
        session.setId("team-1");
        session.setTeamId("team-1");
        session.getMembers().add("user-1");
        session.setStatus("active");
        session.getEvidenceMap().put("clue", 2);
        TrustEntity trust = new TrustEntity();
        trust.setTrustScore(1);

        when(userRepository.findByUsername("agent")).thenReturn(user);
        when(teamSessionRepository.findById("team-1")).thenReturn(Optional.of(session));
        when(trustService.updateTrustScore("user-1", "TEAM_team-1", 1)).thenReturn(trust);
        when(teamSessionRepository.save(any(TeamSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TeamSession updated = gameplayConsequenceService.applyTeamEvidence("team-1", "agent", "clue");

        assertThat(updated.getEvidenceMap().get("clue")).isEqualTo(3);
        assertThat(updated.getStatus()).isEqualTo("ACCUSATION_UNLOCKED");
        verify(teamSessionRepository).save(session);
    }

    @Test
    void teamAccusation_PersistsResultAndTrustConsequence() {
        User user = user();
        TeamSession session = new TeamSession();
        session.setId("team-1");
        session.setTeamId("team-1");
        session.getMembers().add("user-1");
        session.setStatus("ACCUSATION_UNLOCKED");
        session.setTraitorId("sable");
        TrustEntity trust = new TrustEntity();
        trust.setTrustScore(5);

        when(userRepository.findByUsername("agent")).thenReturn(user);
        when(teamSessionRepository.findById("team-1")).thenReturn(Optional.of(session));
        when(trustService.updateTrustScore("user-1", "TEAM_team-1", 5)).thenReturn(trust);
        when(teamSessionRepository.save(any(TeamSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TeamSession updated = gameplayConsequenceService.applyTeamAccusation("team-1", "agent", "sable");

        assertThat(updated.getAccusationResult()).isEqualTo("CORRECT");
        assertThat(updated.getStatus()).isEqualTo("ACCUSATION_RESOLVED");
        verify(teamSessionRepository).save(session);
    }

    @Test
    void teamEvidence_RejectsNonMemberMutation() {
        User user = user();
        TeamSession session = new TeamSession();
        session.setId("team-1");
        session.setTeamId("team-1");
        session.setStatus("active");

        when(userRepository.findByUsername("agent")).thenReturn(user);
        when(teamSessionRepository.findById("team-1")).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> gameplayConsequenceService.applyTeamEvidence("team-1", "agent", "clue"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("not a member");
        verify(trustService, never()).updateTrustScore(anyString(), anyString(), anyInt());
    }

    @Test
    void teamAccusation_RejectsRepeatAfterResolution() {
        User user = user();
        TeamSession session = new TeamSession();
        session.setId("team-1");
        session.setTeamId("team-1");
        session.getMembers().add("user-1");
        session.setStatus("ACCUSATION_RESOLVED");

        when(userRepository.findByUsername("agent")).thenReturn(user);
        when(teamSessionRepository.findById("team-1")).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> gameplayConsequenceService.applyTeamAccusation("team-1", "agent", "sable"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not unlocked");
        verify(trustService, never()).updateTrustScore(anyString(), anyString(), anyInt());
    }

    private User user() {
        User user = new User();
        user.setId("user-1");
        user.setUsername("agent");
        return user;
    }

    private Operator operator() {
        Operator operator = new Operator();
        operator.setId("op_analyst");
        operator.setName("Analyst");
        operator.setRole("Analyst");
        return operator;
    }

    private Mission mission() {
        Mission mission = new Mission();
        mission.setId("mission-web");
        mission.setTitle("Web Perimeter");
        mission.setMissionType("web");
        mission.setDifficulty("easy");
        return mission;
    }
}
