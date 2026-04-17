package com.shadownet.nexus.service;

import com.shadownet.nexus.entity.TeamSession;
import com.shadownet.nexus.repository.TeamSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamSessionServiceTest {

    @Mock private TeamSessionRepository teamSessionRepository;
    @Mock private GameplayConsequenceService gameplayConsequenceService;

    @InjectMocks private TeamSessionService teamSessionService;

    @Test
    void createTeam_PersistsLeaderReadyStateMissionAndActivity() {
        when(teamSessionRepository.save(any(TeamSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TeamSession session = teamSessionService.createTeam("leader-1", "mission-web");

        assertThat(session.getTeamId()).startsWith("team-");
        assertThat(session.getMissionId()).isEqualTo("mission-web");
        assertThat(session.getLeaderId()).isEqualTo("leader-1");
        assertThat(session.getMembers()).containsExactly("leader-1");
        assertThat(session.getReadyMap()).containsEntry("leader-1", false);
        assertThat(session.getActivityLog()).extracting(event -> event.get("type")).contains("team:create");
    }

    @Test
    void joinTeam_RejectsAlreadyStartedSession() {
        TeamSession session = team("team-1", "leader-1");
        session.setStatus("active");
        when(teamSessionRepository.findById("team-1")).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> teamSessionService.joinTeam("team-1", "user-2"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already started");
    }

    @Test
    void getTeamForUser_RejectsNonMemberRead() {
        TeamSession session = team("team-1", "leader-1");
        when(teamSessionRepository.findById("team-1")).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> teamSessionService.getTeamForUser("team-1", "intruder"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("not a member");
    }

    @Test
    void startTeam_RequiresLeaderAndAllMembersReady() {
        TeamSession session = team("team-1", "leader-1");
        session.getMembers().add("user-2");
        session.getReadyMap().put("leader-1", true);
        session.getReadyMap().put("user-2", false);
        when(teamSessionRepository.findById("team-1")).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> teamSessionService.startTeam("team-1", "leader-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All team members");

        session.getReadyMap().put("user-2", true);
        when(teamSessionRepository.save(any(TeamSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TeamSession started = teamSessionService.startTeam("team-1", "leader-1");

        assertThat(started.getStatus()).isEqualTo("active");
        assertThat(started.getTimeStarted()).isNotNull();
        assertThat(started.getActivityLog()).extracting(event -> event.get("type")).contains("team:start");
    }

    private TeamSession team(String teamId, String leaderId) {
        TeamSession session = new TeamSession();
        session.setId(teamId);
        session.setTeamId(teamId);
        session.setStatus("waiting");
        session.setLeaderId(leaderId);
        session.getMembers().add(leaderId);
        session.getReadyMap().put(leaderId, false);
        return session;
    }
}
