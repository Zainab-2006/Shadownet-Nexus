package com.shadownet.nexus.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shadownet.nexus.entity.UserEvent;
import com.shadownet.nexus.repository.UserEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EventService {

    @Autowired
    private UserEventRepository eventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Log user interaction event (Pillar 4 core)
     */
    public void logEvent(String userId, String eventType, String challengeId, String category,
            Map<String, Object> metadata) {
        UserEvent event = new UserEvent(userId, eventType, challengeId, category, metadata);
        eventRepository.save(event);
    }

    /**
     * Simplified risk evaluation (hourly window)
     */
    public RiskLevel evaluateRisk(String userId) {
        long cutoff = System.currentTimeMillis() - 3600000; // 1 hour

        long fails = eventRepository.countRecentFails(userId, cutoff);
        long attempts = eventRepository.countRecentAttempts(userId, cutoff);

        double failRate = attempts > 0 ? (double) fails / attempts : 0;

        if (fails > 7 || failRate > 0.7)
            return RiskLevel.HIGH;
        if (fails > 3 || failRate > 0.5)
            return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    /**
     * Get recent events for dashboard/debug
     */
    public List<UserEvent> getRecentEvents(String userId, int limit) {
        return eventRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
                userId, System.currentTimeMillis() - 86400000L, PageRequest.of(0, limit));
    }

    /**
     * Trigger early intervention notification
     */
    public void checkAndTriggerIntervention(String userId) {
        RiskLevel risk = evaluateRisk(userId);
        if (risk == RiskLevel.HIGH) {
            // TODO: Integrate notification service
            logEvent(userId, "risk_alert", null, null,
                    Map.of("riskLevel", "HIGH", "failures",
                            eventRepository.countRecentFails(userId, System.currentTimeMillis() - 3600000)));
        }
    }
}

enum RiskLevel {
    LOW, MEDIUM, HIGH
}
