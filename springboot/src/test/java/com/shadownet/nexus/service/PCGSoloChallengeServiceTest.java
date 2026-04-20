package com.shadownet.nexus.service;

import com.shadownet.nexus.dto.PCGChallengeGenerateRequest;
import com.shadownet.nexus.dto.PCGChallengeSubmitRequest;
import com.shadownet.nexus.entity.PCGChallengeInstance;
import com.shadownet.nexus.repository.PCGChallengeInstanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PCGSoloChallengeServiceTest {

    @Mock
    private PCGChallengeInstanceRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ScoreAwardService scoreAwardService;

    @Mock
    private EventService eventService;

    @Mock
    private AdaptiveEngineService adaptiveEngineService;

    @InjectMocks
    private PCGSoloChallengeService service;

    @Test
    void generateOrReuseChallenge_ReturnsSafeViewAndStoresHash() {
        PCGChallengeGenerateRequest request = new PCGChallengeGenerateRequest();
        request.setSessionId("session-1");
        request.setCategory("web");
        request.setDifficulty("easy");

        when(repository.findFirstByUserIdAndSessionIdAndStatus("user-1", "session-1", "ACTIVE"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-flag");

        var dto = service.generateOrReuseChallenge("user-1", request);

        assertThat(dto.getInstanceKey()).startsWith("solo_session-1_");
        assertThat(dto.getCategory()).isEqualTo("web");
        assertThat(dto.getDifficulty()).isEqualTo("easy");
        assertThat(dto.getPoints()).isEqualTo(100);
        assertThat(dto.getArtifactJson()).doesNotContain("CTF{");

        ArgumentCaptor<PCGChallengeInstance> captor = ArgumentCaptor.forClass(PCGChallengeInstance.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getFlagHash()).isEqualTo("encoded-flag");
    }

    @Test
    void generateOrReuseChallenge_ReusesActiveInstance() {
        PCGChallengeInstance existing = activeInstance();
        when(repository.findFirstByUserIdAndSessionIdAndStatus("user-1", "session-1", "ACTIVE"))
                .thenReturn(Optional.of(existing));

        PCGChallengeGenerateRequest request = new PCGChallengeGenerateRequest();
        request.setSessionId("session-1");

        var dto = service.generateOrReuseChallenge("user-1", request);

        assertThat(dto.getInstanceKey()).isEqualTo("solo_session-1_123");
        verify(repository, never()).save(any());
    }

    @Test
    void submitChallenge_WithWrongFlag_DoesNotAwardPoints() {
        PCGChallengeInstance existing = activeInstance();
        when(repository.findByInstanceKey("solo_session-1_123")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("wrong", "encoded-flag")).thenReturn(false);

        PCGChallengeSubmitRequest request = new PCGChallengeSubmitRequest();
        request.setInstanceKey("solo_session-1_123");
        request.setSubmittedFlag("wrong");

        var response = service.submitChallenge("user-1", request);

        assertThat(response.isCorrect()).isFalse();
        assertThat(response.getPointsAwarded()).isZero();
        assertThat(existing.getAttemptCount()).isEqualTo(1);
        verify(scoreAwardService, never()).awardSoloPCGPoints(anyString(), anyInt());
        verify(adaptiveEngineService).updateSkill("user-1", "web", false, 0);
    }

    @Test
    void submitChallenge_WithCorrectFlag_AwardsPointsOnce() {
        PCGChallengeInstance existing = activeInstance();
        when(repository.findByInstanceKey("solo_session-1_123")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("correct", "encoded-flag")).thenReturn(true);

        PCGChallengeSubmitRequest request = new PCGChallengeSubmitRequest();
        request.setInstanceKey("solo_session-1_123");
        request.setSubmittedFlag("correct");

        var response = service.submitChallenge("user-1", request);

        assertThat(response.isCorrect()).isTrue();
        assertThat(response.getPointsAwarded()).isEqualTo(100);
        assertThat(existing.getStatus()).isEqualTo("SOLVED");
        verify(scoreAwardService).awardSoloPCGPoints("user-1", 100);
        verify(adaptiveEngineService).updateSkill("user-1", "web", true, 0);
    }

    private PCGChallengeInstance activeInstance() {
        PCGChallengeInstance instance = new PCGChallengeInstance();
        instance.setInstanceKey("solo_session-1_123");
        instance.setUserId("user-1");
        instance.setSessionId("session-1");
        instance.setSeed(123L);
        instance.setCategory("web");
        instance.setDifficulty("easy");
        instance.setTitle("Dynamic Web Challenge");
        instance.setDescription("Generated challenge");
        instance.setArtifactJson("{\"seed\":123}");
        instance.setFlagHash("encoded-flag");
        instance.setPoints(100);
        instance.setAttemptCount(0);
        instance.setHintsUsed(0);
        instance.setStatus("ACTIVE");
        instance.setCreatedAt(LocalDateTime.now());
        instance.setExpiresAt(LocalDateTime.now().plusHours(2));
        return instance;
    }
}
