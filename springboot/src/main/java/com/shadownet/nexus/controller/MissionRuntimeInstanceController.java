package com.shadownet.nexus.controller;

import com.shadownet.nexus.dto.*;
import com.shadownet.nexus.service.MissionRuntimeInstanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mission-runtime")
public class MissionRuntimeInstanceController {
    private final MissionRuntimeInstanceService service;

    public MissionRuntimeInstanceController(MissionRuntimeInstanceService service) {
        this.service = service;
    }

    @PostMapping("/start")
    public ResponseEntity<?> start(@RequestBody MissionStartRequest request, Authentication auth) {
        try {
            return ResponseEntity.ok(service.startOrReuseMission(auth.getName(), request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{instanceKey}")
    public ResponseEntity<?> get(@PathVariable String instanceKey, Authentication auth) {
        try {
            return ResponseEntity.ok(service.getMission(auth.getName(), instanceKey));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/decision")
    public ResponseEntity<?> decision(@RequestBody MissionDecisionRequest request, Authentication auth) {
        try {
            return ResponseEntity.ok(service.submitDecision(auth.getName(), request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/evidence")
    public ResponseEntity<?> evidence(@RequestBody MissionEvidenceClaimRequest request, Authentication auth) {
        try {
            return ResponseEntity.ok(service.claimEvidence(auth.getName(), request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }
}
