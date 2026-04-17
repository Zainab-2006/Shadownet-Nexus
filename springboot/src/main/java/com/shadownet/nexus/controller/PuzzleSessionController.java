package com.shadownet.nexus.controller;

import com.shadownet.nexus.service.CoachingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/puzzle-session")
@Deprecated
public class PuzzleSessionController {

    @Autowired
    private CoachingService coachingService;

    @Deprecated
    @PostMapping("/session/{challengeId}")
    public ResponseEntity<?> startOrGetSession(
            @PathVariable String challengeId,
            @RequestParam(defaultValue = "op_hacker") String operator,
            @RequestParam(defaultValue = "easy") String difficulty,
            Authentication auth) {

        String userId = auth.getName();
        Map<String, Object> sessionData = new HashMap<>(coachingService.getOrCreateSession(userId, challengeId));
        sessionData.put("sessionId", sessionData.get("id"));
        sessionData.put("operator", operator);
        sessionData.put("difficulty", difficulty);
        sessionData.put("deprecatedRoute", "/api/puzzle-session");
        sessionData.put("canonicalRoute", "/api/puzzle");
        return ResponseEntity.ok(sessionData);
    }

    @Deprecated
    @GetMapping("/session/{challengeId}")
    public ResponseEntity<?> getSession(@PathVariable String challengeId, Authentication auth) {
        String userId = auth.getName();
        return ResponseEntity.ok(coachingService.getOrCreateSession(userId, challengeId));
    }

    @Deprecated
    @PostMapping("/submit")
    public ResponseEntity<?> submitStage(@RequestBody Map<String, Object> body, Authentication auth) {
        String sessionId = (String) body.get("sessionId");
        Integer stageNumber = ((Number) body.get("stageNumber")).intValue();
        String flag = (String) body.get("flag");
        boolean trainingMode = Boolean.TRUE.equals(body.get("trainingMode"));
        boolean rankedEligible = !body.containsKey("rankedEligible") || Boolean.TRUE.equals(body.get("rankedEligible"));
        boolean solutionRevealed = Boolean.TRUE.equals(body.get("solutionRevealed"));
        boolean narratorTriggered = Boolean.TRUE.equals(body.get("narratorTriggered"));

        return ResponseEntity.ok(coachingService.submitStage(auth.getName(), sessionId, stageNumber, flag,
                trainingMode, rankedEligible, solutionRevealed, narratorTriggered));
    }

    @Deprecated
    @PostMapping("/hint")
    public ResponseEntity<?> getHint(@RequestBody Map<String, String> body, Authentication auth) {
        String sessionId = body.get("sessionId");
        return ResponseEntity.ok(coachingService.getHint(auth.getName(), sessionId));
    }
}
