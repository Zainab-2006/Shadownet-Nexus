package com.shadownet.nexus.controller;

import com.shadownet.nexus.config.SecurityConfig;
import com.shadownet.nexus.dto.ErrorResponse;
import com.shadownet.nexus.entity.Challenge;
import com.shadownet.nexus.entity.Solve;
import com.shadownet.nexus.repository.ChallengeRepository;
import com.shadownet.nexus.repository.SolveRepository;
import com.shadownet.nexus.service.GameService;
import com.shadownet.nexus.util.AuthenticationAuditLogger;
import com.shadownet.nexus.service.CoachingService;
import com.shadownet.nexus.service.DockerManagerService;
import com.shadownet.nexus.service.EventService;
import com.shadownet.nexus.util.InputValidator;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChallengeController {

    private static final Logger logger = LoggerFactory.getLogger(ChallengeController.class);

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private SolveRepository solveRepository;

    @Autowired
    private GameService gameService;

    @Autowired
    private AuthenticationAuditLogger auditLogger;

    @Autowired
    private CoachingService coachingService;

    @Autowired
    private DockerManagerService dockerManagerService;

    @Autowired
    private EventService eventService;

    /**
     * Get all challenges (no IDOR - all users can see all challenges)
     */
    @GetMapping("/challenges")
    public ResponseEntity<?> getChallenges(Authentication auth, HttpServletRequest request) {
        try {
            String userId = auth != null ? auth.getName() : "anonymous";
            if (auth != null) {
                auditLogger.logDataAccess(userId, "CHALLENGE", "*", "LIST", getClientIpAddress(request));
            }

            List<Challenge> challenges = challengeRepository.findAll();
            return ResponseEntity.ok(challenges);
        } catch (Exception e) {
            logger.error("Error retrieving challenges: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Unable to retrieve challenges", 500));
        }
    }

    /**
     * Search challenges with input validation
     */
    @GetMapping("/search/challenges")
    public ResponseEntity<?> searchChallenges(
            @RequestParam String q,
            Authentication auth,
            HttpServletRequest request) {
        try {
            String userId = auth != null ? auth.getName() : "anonymous";

            // Input validation - prevent injection attacks
            if (InputValidator.containsSqlInjectionAttempt(q) ||
                    InputValidator.containsXssAttempt(q)) {
                auditLogger.logSuspiciousInput(userId, "/api/search/challenges", "SQL_INJECTION",
                        getClientIpAddress(request));
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("INVALID_INPUT", "Invalid search query", 403));
            }

            // Sanitize search query
            String sanitizedQuery = InputValidator.sanitizeInput(q).toLowerCase();

            // Limit search results
            List<Challenge> results = challengeRepository.findAll().stream()
                    .filter(c -> c.getName().toLowerCase().contains(sanitizedQuery) ||
                            c.getDescription().toLowerCase().contains(sanitizedQuery))
                    .limit(50) // Max 50 results
                    .toList();

            auditLogger.logDataAccess(userId, "CHALLENGE", "SEARCH", "SEARCH_QUERY: " + sanitizedQuery,
                    getClientIpAddress(request));
            return ResponseEntity.ok(Map.of("query", q, "results", results));

        } catch (Exception e) {
            logger.error("Error searching challenges: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Search failed", 500));
        }
    }

    /**
     * Submit flag with rate limiting, input validation, and IDOR prevention
     */
    @PostMapping("/submit-flag")
    public ResponseEntity<?> submitFlag(
            @RequestBody Map<String, String> body,
            Authentication auth,
            HttpServletRequest request) {
        try {
            String userId = auth.getName();
            String ipAddress = getClientIpAddress(request);

            // Rate limiting: 10 flag submissions per minute per user
            Bucket bucket = SecurityConfig.getRateLimitingBucket(userId + ":submit-flag");
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            if (!probe.isConsumed()) {
                auditLogger.logSuspiciousInput(userId, "/api/submit-flag", "RATE_LIMIT", ipAddress);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(new ErrorResponse("RATE_LIMIT_EXCEEDED",
                                "Too many flag submissions. Please try again later.",
                                429));
            }

            // Input validation
            String challengeId = body.get("challengeId");
            String flag = body.get("flag");
            boolean trainingMode = Boolean.parseBoolean(body.getOrDefault("trainingMode", "false"));
            boolean rankedEligible = Boolean.parseBoolean(body.getOrDefault("rankedEligible", "true"));
            boolean solutionRevealed = Boolean.parseBoolean(body.getOrDefault("solutionRevealed", "false"));
            boolean narratorTriggered = Boolean.parseBoolean(body.getOrDefault("narratorTriggered", "false"));

            if (!InputValidator.isValidChallengeId(challengeId)) {
                auditLogger.logSuspiciousInput(userId, "/api/submit-flag", "INVALID_CHALLENGE_ID", ipAddress);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("INVALID_INPUT", "Invalid challenge ID", 403));
            }

            if (!InputValidator.isValidFlag(flag)) {
                auditLogger.logSuspiciousInput(userId, "/api/submit-flag", "INVALID_FLAG", ipAddress);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("INVALID_INPUT", "Invalid flag format", 403));
            }

            // Verify challenge exists (IDOR prevention)
            Challenge challenge = challengeRepository.findById(challengeId).orElse(null);
            if (challenge == null) {
                auditLogger.logSuspiciousInput(userId, "/api/submit-flag", "CHALLENGE_NOT_FOUND", ipAddress);
                return ResponseEntity.notFound().build();
            }

            // Check if user has already solved this (prevent duplicate submissions)
            boolean alreadySolved = solveRepository.existsByUserIdAndChallengeId(userId, challengeId);
            if (alreadySolved) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Challenge already solved", "success", false));
            }

            // Submit flag with dynamic scoring
            int result = gameService.submitFlag(
                    userId,
                    challengeId,
                    flag,
                    trainingMode,
                    rankedEligible,
                    solutionRevealed,
                    narratorTriggered);

            if (result > 0) {
                auditLogger.logDataAccess(userId, "CHALLENGE", challengeId, "FLAG_SUBMITTED_SUCCESS", ipAddress);
                return ResponseEntity.ok(Map.of("success", true, "message", "Flag accepted!", "points", result));
            } else if (result == 0 && (trainingMode || !rankedEligible || solutionRevealed || narratorTriggered)) {
                auditLogger.logDataAccess(userId, "CHALLENGE", challengeId, "FLAG_SUBMITTED_TRAINING", ipAddress);
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Flag accepted as training solve. No ranked points awarded.",
                        "points", 0,
                        "ranked", false,
                        "trainingMode", trainingMode,
                        "rankedEligible", rankedEligible));
            } else if (result == -1) {
                auditLogger.logDataAccess(userId, "CHALLENGE", challengeId, "FLAG_INVALID", ipAddress);
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Invalid flag"));
            } else if (result == -2) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Already solved"));
            } else {
                auditLogger.logDataAccess(userId, "CHALLENGE", challengeId, "FLAG_SUBMITTED_FAILED", ipAddress);
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Submission failed"));
            }

        } catch (Exception e) {
            logger.error("Error submitting flag: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Flag submission failed", 500));
        }
    }

    /**
     * Pillar 3: Get or create puzzle session for challenge
     */
    @GetMapping("/puzzle/session/{challengeId}")
    public ResponseEntity<?> getPuzzleSession(
            @PathVariable String challengeId,
            Authentication auth,
            HttpServletRequest request) {
        try {
            String userId = auth.getName();
            Map<String, Object> sessionData = coachingService.getOrCreateSession(userId, challengeId);
            auditLogger.logDataAccess(userId, "PUZZLE_SESSION", challengeId, "VIEW", getClientIpAddress(request));
            return ResponseEntity.ok(sessionData);
        } catch (Exception e) {
            logger.error("Error getting puzzle session: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get hint for puzzle session
     */
    @PostMapping("/puzzle/hint")
    public ResponseEntity<?> getPuzzleHint(
            @RequestBody Map<String, String> body,
            Authentication auth,
            HttpServletRequest request) {
        try {
            String userId = auth.getName();
            String sessionId = body.get("sessionId");
            Map<String, Object> hintData = coachingService.getHint(userId, sessionId);
            auditLogger.logDataAccess(userId, "PUZZLE_HINT", sessionId, "REQUEST", getClientIpAddress(request));
            return ResponseEntity.ok(hintData);
        } catch (Exception e) {
            logger.error("Error getting hint: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Submit stage flag
     */
    @PostMapping("/puzzle/submit")
    public ResponseEntity<?> submitPuzzleStage(
            @RequestBody Map<String, Object> body,
            Authentication auth,
            HttpServletRequest request) {
        try {
            String userId = auth.getName();
            String sessionId = (String) body.get("sessionId");
            int stageNumber = ((Number) body.get("stageNumber")).intValue();
            String flag = (String) body.get("flag");
            boolean trainingMode = Boolean.TRUE.equals(body.get("trainingMode"));
            boolean rankedEligible = !body.containsKey("rankedEligible") || Boolean.TRUE.equals(body.get("rankedEligible"));
            boolean solutionRevealed = Boolean.TRUE.equals(body.get("solutionRevealed"));
            boolean narratorTriggered = Boolean.TRUE.equals(body.get("narratorTriggered"));
            Map<String, Object> result = coachingService.submitStage(
                    userId,
                    sessionId,
                    stageNumber,
                    flag,
                    trainingMode,
                    rankedEligible,
                    solutionRevealed,
                    narratorTriggered);
            auditLogger.logDataAccess(userId, "PUZZLE_SUBMIT", sessionId, "STAGE_" + stageNumber,
                    getClientIpAddress(request));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error submitting puzzle stage: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Pillar 5: Spawn Docker container for challenge (Web/Pwn only)
     */
    @PostMapping("/challenges/{challengeId}/spawn")
    public ResponseEntity<?> spawnChallengeContainer(
            @PathVariable String challengeId,
            Authentication auth,
            HttpServletRequest request) {
        try {
            String userId = auth.getName();
            Map<String, String> result = dockerManagerService.spawnChallengeInstance(userId, challengeId);

            String ipAddress = getClientIpAddress(request);
            auditLogger.logDataAccess(userId, "CONTAINER_SPAWN", challengeId, "SUCCESS", ipAddress);

            eventService.logEvent(userId, "container_spawn", challengeId, null,
                    Map.of("containerUrl", result.get("url"), "demoMode", result.get("demoMode")));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Container spawn failed for challenge {}: {}", challengeId, e.getMessage());
            auditLogger.logDataAccess(auth.getName(), "CONTAINER_SPAWN", challengeId, "FAILED",
                    getClientIpAddress(request));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Container spawn failed: " + e.getMessage()));
        }
    }

    /**
     * Start next puzzle for solo mode (legacy)
     */
    @PostMapping("/puzzle/start")
    public ResponseEntity<?> startPuzzle(Authentication auth, HttpServletRequest request) {
        try {
            String userId = auth.getName();
            Challenge challenge = gameService.getNextUnsolvedChallenge(userId);
            if (challenge == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No puzzles available"));
            }
            auditLogger.logDataAccess(userId, "PUZZLE", challenge.getId(), "START", getClientIpAddress(request));
            return ResponseEntity.ok(Map.of("challenge", challenge));
        } catch (Exception e) {
            logger.error("Error starting puzzle: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal error"));
        }
    }

    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
