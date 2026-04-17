package com.shadownet.nexus.controller;

import com.shadownet.nexus.entity.Challenge;
import com.shadownet.nexus.service.PCGService;
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

    @PostMapping("/generate")
    public ResponseEntity<?> generateChallenge(@RequestBody Map<String, Object> body, Authentication auth) {
        try {
            long seed = ((Number) body.get("seed")).longValue();
            String sessionId = (String) body.get("sessionId");
            String userId = auth.getName();

            Challenge challenge = pcgService.generateDynamicChallenge(seed, sessionId);
            logger.info("PCG challenge generated for user {} session {} seed {}", userId, sessionId, seed);
            return ResponseEntity.ok(challenge);
        } catch (Exception e) {
            logger.error("Error generating PCG challenge: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate challenge"));
        }
    }
}
