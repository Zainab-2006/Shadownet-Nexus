package com.shadownet.nexus.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shadownet.nexus.entity.Challenge;
import com.shadownet.nexus.entity.PuzzleSession;
import com.shadownet.nexus.mapper.ChallengeViewMapper;
import com.shadownet.nexus.repository.ChallengeRepository;
import com.shadownet.nexus.repository.PuzzleSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CoachingService {

    private static final Logger logger = LoggerFactory.getLogger(CoachingService.class);

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private PuzzleSessionRepository puzzleSessionRepository;

    @Autowired
    private AdaptiveEngineService adaptiveEngineService;

    @Autowired
    private GameService gameService;

    @Autowired
    private ChallengeViewMapper challengeViewMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> getOrCreateSession(String userId, String challengeId) {
        Optional<PuzzleSession> existing = puzzleSessionRepository.findActiveByUserIdAndChallengeId(userId, challengeId);
        PuzzleSession session = existing.orElseGet(() -> puzzleSessionRepository.save(new PuzzleSession(userId, challengeId)));

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("id", session.getId());
        response.put("currentStage", session.getCurrentStage());
        response.put("hintsUsed", session.getHintsUsed());
        response.put("challenge", challengeViewMapper.toPuzzleDto(challenge));
        response.put("completed", session.isCompleted());
        return response;
    }

    public Map<String, Object> getHint(String sessionId) {
        PuzzleSession session = puzzleSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (session.getHintsUsed() >= 3) {
            throw new IllegalStateException("Max hints reached (3)");
        }

        Challenge challenge = challengeRepository.findById(session.getChallengeId()).orElse(null);
        if (challenge == null) {
            throw new IllegalArgumentException("Challenge not found");
        }

        try {
            int hintIndex = session.getHintsUsed();
            String baseHint = resolveHintContent(challenge, hintIndex);
            String weakness = getWeakness(session.getUserId(), challenge.getCategory());

            String personalizedHint = baseHint;
            if (weakness != null) {
                personalizedHint += " (Tip for your " + weakness + " skill: " + getCategoryTip(challenge.getCategory(), weakness) + ")";
            }

            puzzleSessionRepository.incrementHintsUsed(sessionId, session.getHintsUsed() + 1, System.currentTimeMillis());

            Map<String, Object> response = new HashMap<>();
            response.put("content", personalizedHint);
            response.put("personalized", weakness != null);
            response.put("remainingHints", 3 - (session.getHintsUsed() + 1));
            return response;
        } catch (Exception e) {
            logger.error("Error generating hint", e);
            throw new RuntimeException("Hint generation failed", e);
        }
    }

    public Map<String, Object> getHint(String userId, String sessionId) {
        PuzzleSession session = puzzleSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (!session.getUserId().equals(userId)) {
            throw new SecurityException("Session does not belong to current user");
        }
        return getHint(sessionId);
    }

    public Map<String, Object> submitStage(String sessionId, int stageNumber, String flag) {
        return submitStage(sessionId, stageNumber, flag, false, true, false, false);
    }

    public Map<String, Object> submitStage(
            String sessionId,
            int stageNumber,
            String flag,
            boolean trainingMode,
            boolean rankedEligible,
            boolean solutionRevealed,
            boolean narratorTriggered) {
        PuzzleSession session = puzzleSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        Challenge challenge = challengeRepository.findById(session.getChallengeId()).orElse(null);
        if (challenge == null) {
            throw new IllegalArgumentException("Challenge not found");
        }

        try {
            JsonNode stagesArray = objectMapper.readTree(challenge.getStages());
            JsonNode stage = stagesArray.get(stageNumber - 1);
            String correctFlagHash = stage.get("flagHash").asText();
            String submittedHash = gameService.hashFlag(flag);

            Map<String, Object> response = new HashMap<>();
            if (!correctFlagHash.equals(submittedHash)) {
                response.put("correct", false);
                response.put("message", "Incorrect flag for this stage. Try again.");
                return response;
            }

            if (stageNumber >= stagesArray.size()) {
                session.setCompleted(true);
                puzzleSessionRepository.completeSession(sessionId, System.currentTimeMillis());
                boolean rankedSolve = !trainingMode && rankedEligible && !solutionRevealed && !narratorTriggered;
                int awardedPoints = rankedSolve ? gameService.completeChallenge(session.getUserId(), challenge.getId()) : 0;
                response.put("correct", true);
                response.put("message", "Challenge completed! Check learning objectives.");
                response.put("showExplanation", true);
                response.put("awardedPoints", Math.max(awardedPoints, 0));
                response.put("ranked", rankedSolve);
                response.put("trainingMode", trainingMode);
                response.put("rankedEligible", rankedEligible);
            } else {
                session.setCurrentStage(stageNumber + 1);
                puzzleSessionRepository.advanceStage(sessionId, stageNumber + 1, System.currentTimeMillis());
                response.put("correct", true);
                response.put("message", "Stage " + stageNumber + " complete! Proceed to next.");
                response.put("nextStage", stageNumber + 1);
            }

            return response;
        } catch (Exception e) {
            logger.error("Error submitting stage", e);
            throw new RuntimeException("Stage submission failed", e);
        }
    }

    public Map<String, Object> submitStage(String userId, String sessionId, int stageNumber, String flag) {
        return submitStage(userId, sessionId, stageNumber, flag, false, true, false, false);
    }

    public Map<String, Object> submitStage(
            String userId,
            String sessionId,
            int stageNumber,
            String flag,
            boolean trainingMode,
            boolean rankedEligible,
            boolean solutionRevealed,
            boolean narratorTriggered) {
        PuzzleSession session = puzzleSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (!session.getUserId().equals(userId)) {
            throw new SecurityException("Session does not belong to current user");
        }
        if (session.isCompleted()) {
            return Map.of("correct", false, "message", "Session already completed.", "duplicate", true);
        }
        if (stageNumber != session.getCurrentStage()) {
            return Map.of("correct", false, "message", "Stage is not active for this session.", "stale", true);
        }
        return submitStage(sessionId, stageNumber, flag, trainingMode, rankedEligible, solutionRevealed, narratorTriggered);
    }

    private String resolveHintContent(Challenge challenge, int hintIndex) throws Exception {
        if (challenge.getHints() != null && !challenge.getHints().isBlank()) {
            try {
                JsonNode hintsArray = objectMapper.readTree(challenge.getHints());
                if (hintsArray.isArray() && hintIndex < hintsArray.size()) {
                    JsonNode hintNode = hintsArray.get(hintIndex);
                    if (hintNode.isTextual()) {
                        return hintNode.asText();
                    }
                    JsonNode content = hintNode.get("content");
                    if (content != null && !content.asText().isBlank()) {
                        return content.asText();
                    }
                }
            } catch (Exception ignored) {
                logger.warn("Ignoring malformed hints for challenge {}", challenge.getId());
            }
        }

        if (challenge.getStages() != null && !challenge.getStages().isBlank()) {
            try {
                JsonNode stagesArray = objectMapper.readTree(challenge.getStages());
                if (stagesArray.isArray() && stagesArray.size() > 0) {
                    JsonNode activeStage = stagesArray.get(0);
                    JsonNode briefing = activeStage.get("briefing");
                    if (briefing != null && !briefing.asText().isBlank()) {
                        return "Review the mission briefing: " + briefing.asText();
                    }
                }
            } catch (Exception ignored) {
                logger.warn("Ignoring malformed stages for challenge {}", challenge.getId());
            }
        }

        return "Review the challenge description and focus on the category fundamentals.";
    }

    private String getWeakness(String userId, String category) {
        return switch (category) {
            case "web" -> "sqli";
            case "crypto" -> "weak_key";
            default -> null;
        };
    }

    private String getCategoryTip(String category, String weakness) {
        return switch (category) {
            case "web" -> switch (weakness) {
                case "sqli" -> "Use prepared statements, never concatenate strings in queries.";
                case "xss" -> "Always escape user input. Use CSP headers.";
                default -> "Review fundamentals.";
            };
            case "crypto" -> switch (weakness) {
                case "weak_key" -> "Use cryptographically secure random for keys.";
                case "no_auth" -> "Always use HMAC or signatures for integrity.";
                default -> "Review fundamentals.";
            };
            default -> "Review fundamentals.";
        };
    }
}
