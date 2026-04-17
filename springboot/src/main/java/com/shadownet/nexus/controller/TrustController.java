package com.shadownet.nexus.controller;

import com.shadownet.nexus.dto.ErrorResponse;
import com.shadownet.nexus.service.TrustService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/trust")
public class TrustController {

    private static final Logger logger = LoggerFactory.getLogger(TrustController.class);

    @Autowired
    private TrustService trustService;

    @GetMapping("/{targetUserId}")
    public ResponseEntity<?> getTrust(Authentication auth, @PathVariable String targetUserId) {
        try {
            String userId = auth.getName();
            var trust = trustService.getOrCreateTrust(userId, targetUserId);
            return ResponseEntity.ok(Map.of("trustScore", trust.getTrustScore()));
        } catch (Exception e) {
            logger.error("Error getting trust: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("ERROR", e.getMessage(), 500));
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateTrust(@RequestBody Map<String, Object> body, Authentication auth) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(new ErrorResponse(
                        "TRUST_UPDATE_DEPRECATED",
                        "Client-authored trust deltas are retired. Use backend-authored consequence endpoints.",
                        410));
    }

    @PostMapping("/calculate")
    public ResponseEntity<?> calculateTrust(@RequestBody Map<String, Object> body, Authentication auth) {
        try {
            String userId = auth.getName();
            String targetUserId = (String) body.get("targetUserId");
            long seed = ((Number) body.get("seed")).longValue();
            int trustScore = trustService.calculateTrust(userId, targetUserId, seed);
            return ResponseEntity.ok(Map.of("trustScore", trustScore));
        } catch (Exception e) {
            logger.error("Error calculating trust: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("ERROR", e.getMessage(), 500));
        }
    }

    @PostMapping("/accuse")
    public ResponseEntity<?> accuse(@RequestBody Map<String, String> body, Authentication auth) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(new ErrorResponse(
                        "TRUST_ACCUSE_DEPRECATED",
                        "Client-authored trust accusation is retired. Use team consequence endpoints.",
                        410));
    }
}
