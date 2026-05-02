package com.shadownet.nexus.service;

import com.shadownet.nexus.entity.TeamSession;
import com.shadownet.nexus.repository.TeamSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
public class EvidenceService {

    @Autowired
    private TeamSessionRepository teamSessionRepository;

    /**
     * Legacy compatibility surface only. Team evidence mutates trust/session state
     * and must be authored through GameplayConsequenceService with a user context.
     * 
     * @deprecated Since 1.5.0. Use
     *             GameplayConsequenceService.addEvidenceToSession() instead.
     *             This method will be removed in v2.0.0.
     *             See migration guide: docs/v2-migration.md#evidence-service
     */
    @Deprecated(since = "1.5.0", forRemoval = true)
    public void addEvidence(String teamId, String evidenceData) {
        throw new UnsupportedOperationException(
                "Team evidence mutation requires GameplayConsequenceService user context");
    }

    public Map<String, Integer> getEvidence(String teamId) {
        TeamSession session = teamSessionRepository.findById(teamId).orElse(null);
        return session != null ? session.getEvidenceMap() : Map.of();
    }
}
