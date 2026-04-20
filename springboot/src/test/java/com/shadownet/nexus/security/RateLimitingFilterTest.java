package com.shadownet.nexus.security;

import com.shadownet.nexus.service.RateLimitService;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RateLimitingFilterTest {
    private final RateLimitingFilter filter = new RateLimitingFilter(mock(RateLimitService.class));

    @Test
    void mapsLiveSubmitRoutesToChallengeSubmitLimit() throws Exception {
        assertThat(rateLimitType("/api/submit-flag")).isEqualTo(RateLimitingFilter.RateLimitType.CHALLENGE_SUBMIT);
        assertThat(rateLimitType("/api/puzzle/submit")).isEqualTo(RateLimitingFilter.RateLimitType.CHALLENGE_SUBMIT);
    }

    @Test
    void mapsLiveHintRoutesToHintLimit() throws Exception {
        assertThat(rateLimitType("/api/puzzle/hint")).isEqualTo(RateLimitingFilter.RateLimitType.HINT);
    }

    @Test
    void mapsContainerSpawnAndTeamRoutes() throws Exception {
        assertThat(rateLimitType("/api/challenges/web-001/spawn")).isEqualTo(RateLimitingFilter.RateLimitType.CONTAINER_SPAWN);
        assertThat(rateLimitType("/api/team/abc/join")).isEqualTo(RateLimitingFilter.RateLimitType.TEAM_ACTION);
    }

    private RateLimitingFilter.RateLimitType rateLimitType(String path) throws Exception {
        Method method = RateLimitingFilter.class.getDeclaredMethod("getRateLimitType", String.class);
        method.setAccessible(true);
        return (RateLimitingFilter.RateLimitType) method.invoke(filter, path);
    }
}
