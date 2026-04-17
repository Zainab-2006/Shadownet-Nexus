package com.shadownet.nexus.controller;

import com.shadownet.nexus.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class LegacyMutationControllerTest {

    @InjectMocks private TrustController trustController;
    @InjectMocks private MissionController missionController;

    @Test
    void trustUpdate_IsGoneAndDoesNotAcceptClientDelta() {
        var response = trustController.updateTrust(Map.of("targetUserId", "op_analyst", "delta", 1000), null);

        assertThat(response.getStatusCode().value()).isEqualTo(410);
        assertThat(((ErrorResponse) response.getBody()).getError()).isEqualTo("TRUST_UPDATE_DEPRECATED");
    }

    @Test
    void missionProgress_IsGoneAndDoesNotAcceptClientOutcome() {
        var response = missionController.updateMissionProgress(
                "mission-web",
                Map.of("outcome", "force-complete", "trustDelta", 999));

        assertThat(response.getStatusCode().value()).isEqualTo(410);
        assertThat(((ErrorResponse) response.getBody()).getError()).isEqualTo("MISSION_PROGRESS_DEPRECATED");
    }

    @Test
    void trustAccuse_IsGoneAndDoesNotAcceptClientAccusation() {
        var response = trustController.accuse(Map.of("targetUserId", "op_analyst"), null);

        assertThat(response.getStatusCode().value()).isEqualTo(410);
        assertThat(((ErrorResponse) response.getBody()).getError()).isEqualTo("TRUST_ACCUSE_DEPRECATED");
    }
}
