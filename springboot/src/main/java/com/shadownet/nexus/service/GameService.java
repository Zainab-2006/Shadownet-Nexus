package com.shadownet.nexus.service;

import com.shadownet.nexus.entity.Challenge;
import com.shadownet.nexus.entity.Solve;
import com.shadownet.nexus.entity.User;
import com.shadownet.nexus.repository.ChallengeRepository;
import com.shadownet.nexus.repository.SolveRepository;
import com.shadownet.nexus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

@Service
public class GameService {

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private SolveRepository solveRepository;

    @Autowired
    private EventService eventService;

    @Autowired
    private AdaptiveEngineService adaptiveEngineService;

    @Autowired
    private UserRepository userRepository;

    public Challenge getNextUnsolvedChallenge(String userId) {
        for (Challenge challenge : challengeRepository.findAll()) {
            if (!solveRepository.existsByUserIdAndChallengeId(userId, challenge.getId())) {
                return challenge;
            }
        }
        return null;
    }

    public int submitFlag(String userId, String challengeId, String flag) {
        return submitFlag(userId, challengeId, flag, false, true, false, false);
    }

    public int submitFlag(
            String userId,
            String challengeId,
            String flag,
            boolean trainingMode,
            boolean rankedEligible,
            boolean solutionRevealed,
            boolean narratorTriggered) {
        Challenge challenge = challengeRepository.findById(challengeId).orElse(null);
        if (challenge == null) {
            eventService.logEvent(userId, "challenge_error", challengeId, null, Map.of("error", "challenge_not_found"));
            return 0;
        }

        String flagHash = hashFlag(flag);
        boolean isCorrect = challenge.getFlagHash().equals(flagHash);

        Map<String, Object> submitMetadata = Map.of(
                "flag_length", flag.length(),
                "is_correct", isCorrect,
                "base_points", challenge.getPoints(),
                "training_mode", trainingMode,
                "ranked_eligible", rankedEligible,
                "solution_revealed", solutionRevealed,
                "narrator_triggered", narratorTriggered,
                "attempt_time", System.currentTimeMillis());
        eventService.logEvent(userId, isCorrect ? "flag_submit_correct" : "flag_submit_wrong", challengeId,
                challenge.getCategory(), submitMetadata);

        if (!isCorrect) {
            return -1;
        }

        boolean rankedSolve = !trainingMode && rankedEligible && !solutionRevealed && !narratorTriggered;
        if (!rankedSolve) {
            eventService.logEvent(userId, "training_solve_no_score", challengeId, challenge.getCategory(),
                    Map.of(
                            "training_mode", trainingMode,
                            "ranked_eligible", rankedEligible,
                            "solution_revealed", solutionRevealed,
                            "narrator_triggered", narratorTriggered));
            return 0;
        }

        return awardSolve(userId, challenge);
    }

    public int completeChallenge(String userId, String challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId).orElse(null);
        if (challenge == null) {
            return 0;
        }
        return awardSolve(userId, challenge);
    }

    private int awardSolve(String userId, Challenge challenge) {
        if (solveRepository.existsByUserIdAndChallengeId(userId, challenge.getId())) {
            eventService.logEvent(userId, "duplicate_submit", challenge.getId(), challenge.getCategory(), Map.of());
            return -2;
        }

        int solveCount = solveRepository.countByChallengeId(challenge.getId());
        int basePoints = challenge.getPoints() != null ? challenge.getPoints() : 100;
        int awardedPoints = calculateDynamicPoints(basePoints, solveCount, challenge.getMaxSolves());

        if (solveCount == 0 && (challenge.getFirstBloodUserId() == null || challenge.getFirstBloodUserId().isEmpty())) {
            awardedPoints = (int) Math.ceil(awardedPoints * 1.1);
            challenge.setFirstBloodUserId(userId);
            challenge.setFirstBloodAt(System.currentTimeMillis());
            challengeRepository.save(challenge);
            eventService.logEvent(userId, "first_blood", challenge.getId(), challenge.getCategory(), Map.of("bonus", 0.1));
        }

        awardedPoints = applyOperatorBonus(userId, awardedPoints);

        Solve solve = new Solve();
        solve.setUserId(userId);
        solve.setChallengeId(challenge.getId());
        solve.setPoints(awardedPoints);
        solve.setTimestamp(System.currentTimeMillis());
        solveRepository.save(solve);

        updateUserProgress(userId, awardedPoints);
        adaptiveEngineService.updateSkill(userId, challenge.getCategory(), true, 0);

        eventService.logEvent(userId, "challenge_solved", challenge.getId(), challenge.getCategory(),
                Map.of("awarded_points", awardedPoints, "solve_rank", solveCount + 1));

        return awardedPoints;
    }

    private int applyOperatorBonus(String userId, int points) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getSelectedOperator() == null) {
            return points;
        }

        double multiplier = switch (user.getSelectedOperator()) {
            case "op_hacker" -> 1.10;
            case "op_analyst" -> 1.05;
            case "op_field" -> 1.08;
            default -> 1.0;
        };

        return (int) Math.ceil(points * multiplier);
    }

    private void updateUserProgress(String userId, int awardedPoints) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }

        int nextScore = (user.getScore() != null ? user.getScore() : 0) + awardedPoints;
        int nextXp = (user.getXp() != null ? user.getXp() : 0) + awardedPoints;
        int nextLevel = Math.max(1, (nextXp / 500) + 1);

        user.setScore(nextScore);
        user.setXp(nextXp);
        user.setLevel(nextLevel);
        user.setUpdatedAt(System.currentTimeMillis());
        userRepository.save(user);
    }

    private int calculateDynamicPoints(int basePoints, int solveCount, int maxSolves) {
        int safeMaxSolves = maxSolves <= 0 ? 100 : maxSolves;
        double factor = 1.0 + (double) (safeMaxSolves - Math.min(safeMaxSolves, solveCount)) / safeMaxSolves;
        return (int) Math.ceil(basePoints * factor);
    }

    public String hashFlag(String flag) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(flag.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
