package com.shadownet.nexus.service;

import com.shadownet.nexus.entity.TeamSession;
import com.shadownet.nexus.repository.TeamSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccusationService {

    @Autowired
    private TeamSessionRepository teamSessionRepository;

    /**
     * Legacy compatibility surface only. Team accusation mutates trust/session
     * state and must be authored through GameplayConsequenceService with a user
     * context.
     */
    @Deprecated
    public boolean submitAccusation(String teamId, String traitorId) {
        throw new UnsupportedOperationException("Team accusation requires GameplayConsequenceService user context");
    }
}
