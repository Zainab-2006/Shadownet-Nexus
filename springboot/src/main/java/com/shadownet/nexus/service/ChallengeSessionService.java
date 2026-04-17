package com.shadownet.nexus.service;

import com.shadownet.nexus.entity.ChallengeSession;
import com.shadownet.nexus.entity.ChallengeStage;
import com.shadownet.nexus.repository.ChallengeSessionRepository;
import com.shadownet.nexus.repository.ChallengeStageRepository;
import com.shadownet.nexus.util.AuthenticationAuditLogger;
import com.shadownet.nexus.service.GameService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChallengeSessionService {

    @Autowired
    private ChallengeSessionRepository sessionRepository;

    @Autowired
    private ChallengeStageRepository stageRepository;

    @Autowired
    private AuthenticationAuditLogger auditLogger;

    @Transactional
    public String startSession(String userId, String challengeId, String operator, String difficulty) {
        Optional<ChallengeSession> existing = sessionRepository.findByUserIdAndChallengeId(userId, challengeId);
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        String sessionId = "session_" + UUID.randomUUID().toString().substring(0, 8);
        ChallengeSession session = new ChallengeSession();
        session.setId(sessionId);
        session.setUserId(userId);
        session.setChallengeId(challengeId);
        session.setOperator(operator);
        session.setDifficulty(difficulty);
        session.setTimeStarted(System.currentTimeMillis());
        session.setStatus("active");
        session.setCreatedAt(System.currentTimeMillis());
        sessionRepository.save(session);

        auditLogger.logDataAccess(userId, "PUZZLE_SESSION", sessionId, "STARTED", "challenge=" + challengeId);
        return sessionId;
    }

    public Optional<ChallengeSession> getSession(String challengeId, String userId) {
        return sessionRepository.findByUserIdAndChallengeId(userId, challengeId);
    }

    public List<ChallengeStage> getStages(String challengeId) {
        return stageRepository.findByChallengeIdOrderByStageNumberAsc(challengeId);
    }

    @Transactional
    public boolean submitStage(String sessionId, int stageNumber, String flag) {
        Optional<ChallengeSession> optSession = sessionRepository.findById(sessionId);
        if (optSession.isEmpty()) return false;

        ChallengeSession session = optSession.get();
        List<ChallengeStage> stages = getStages(session.getChallengeId());
        if (stages.size() < stageNumber) return false;

        ChallengeStage stage = stages.get(stageNumber - 1);
        String flagHash = new GameService().hashFlag(flag);

        if (!stage.getFlagHash().equals(flagHash)) {
            return false;
        }

        // Advance stage
        session.setCurrentStage(session.getCurrentStage() + 1);
        session.setTotalTime((int) ((System.currentTimeMillis() - session.getTimeStarted()) / 1000));

        // Complete if last stage
        if (session.getCurrentStage() > stages.size()) {
            session.setStatus("completed");
            session.setTimeCompleted(System.currentTimeMillis());
            session.setCompleted(true);
        }

        sessionRepository.save(session);
        return true;
    }

    @Transactional
    public String getHint(String sessionId) {
        Optional<ChallengeSession> optSession = sessionRepository.findById(sessionId);
        if (optSession.isEmpty()) return null;

        ChallengeSession session = optSession.get();
        int hintsUsed = session.getHintsUsed();
        if (hintsUsed >= 3) return null;

        session.setHintsUsed(hintsUsed + 1);
        session.setPenaltyMultiplier(1.0 + (hintsUsed + 1) * 0.1);
        sessionRepository.save(session);

        List<ChallengeStage> stages = getStages(session.getChallengeId());
        ChallengeStage stage = stages.get(session.getCurrentStage() - 1);
        auditLogger.logDataAccess(session.getUserId(), "HINT", sessionId, "STAGE_" + session.getCurrentStage(), "hints=" + hintsUsed);
        return stage.getHint();
    }

    @Transactional
    public void applyOperatorBonus(String sessionId, String operator) {
        Optional<ChallengeSession> optSession = sessionRepository.findById(sessionId);
        if (optSession.isEmpty()) return;

        ChallengeSession session = optSession.get();
        
        switch (operator) {
            case "op_hacker":
                session.setPenaltyMultiplier(Math.max(0.5, session.getPenaltyMultiplier() * 0.8)); // Reduce penalty
                break;
            case "op_engineer":
                session.setHintsUsed(Math.max(0, session.getHintsUsed() - 1)); // Refund hint
                break;
            // Add more operators
            default:
                break;
        }
        
        sessionRepository.save(session);
    }
}
