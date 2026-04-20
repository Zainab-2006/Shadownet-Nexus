package com.shadownet.nexus.controller;

import com.shadownet.nexus.entity.Challenge;
import com.shadownet.nexus.mapper.ChallengeViewMapper;
import com.shadownet.nexus.dto.PCGChallengeGenerateRequest;
import com.shadownet.nexus.dto.PCGChallengeSubmitRequest;
import com.shadownet.nexus.dto.PCGChallengeSubmitResponse;
import com.shadownet.nexus.dto.PCGChallengeViewDTO;
import com.shadownet.nexus.service.PCGService;
import com.shadownet.nexus.service.PCGSoloChallengeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pcg")
public class PCGController {

    private static final Logger logger = LoggerFactory.getLogger(PCGController.class);

    @Autowired
    private PCGService pcgService;

    @Autowired
    private ChallengeViewMapper challengeViewMapper;

    @Autowired
    private PCGSoloChallengeService pcgSoloChallengeService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateChallenge(@RequestBody Map<String, Object> body, Authentication auth) {
        try {
            long seed = ((Number) body.get("seed")).longValue();
            String sessionId = (String) body.get("sessionId");
            String userId = auth.getName();

            Challenge challenge = pcgService.generateDynamicChallenge(seed, sessionId);
            logger.info("PCG challenge generated for user {} session {} seed {}", userId, sessionId, seed);
            return ResponseEntity.ok(challengeViewMapper.toPuzzleDto(challenge));
        } catch (Exception e) {
            logger.error("Error generating PCG challenge: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate challenge"));
        }
    }

    @PostMapping("/solo/generate")
    public ResponseEntity<?> generateSoloChallenge(
            @RequestBody PCGChallengeGenerateRequest request,
            Authentication auth) {
        try {
            PCGChallengeViewDTO challenge = pcgSoloChallengeService.generateOrReuseChallenge(auth.getName(), request);
            logger.info("Solo PCG challenge prepared for user {} session {}", auth.getName(), request.getSessionId());
            return ResponseEntity.ok(challenge);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error generating solo PCG challenge: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate solo PCG challenge"));
        }
    }

    @GetMapping("/solo/{instanceKey}")
    public ResponseEntity<?> getSoloChallenge(
            @PathVariable String instanceKey,
            Authentication auth) {
        try {
            return ResponseEntity.ok(pcgSoloChallengeService.getChallenge(auth.getName(), instanceKey));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/solo/submit")
    public ResponseEntity<?> submitSoloChallenge(
            @RequestBody PCGChallengeSubmitRequest request,
            Authentication auth) {
        try {
            PCGChallengeSubmitResponse response = pcgSoloChallengeService.submitChallenge(auth.getName(), request);
            return response.isCorrect()
                    ? ResponseEntity.ok(response)
                    : ResponseEntity.badRequest().body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error submitting solo PCG challenge: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to submit solo PCG challenge"));
        }
    }
}
