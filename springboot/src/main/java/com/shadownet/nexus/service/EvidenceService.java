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

    public Map<String, Integer> getEvidence(String teamId) {
        TeamSession session = teamSessionRepository.findById(teamId).orElse(null);
        return session != null ? session.getEvidenceMap() : Map.of();
    }
}
