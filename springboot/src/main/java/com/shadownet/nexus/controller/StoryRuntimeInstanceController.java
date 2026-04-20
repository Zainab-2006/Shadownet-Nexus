package com.shadownet.nexus.controller;

import com.shadownet.nexus.dto.*;
import com.shadownet.nexus.service.StoryRuntimeInstanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/story-runtime")
public class StoryRuntimeInstanceController {
    private final StoryRuntimeInstanceService service;

    public StoryRuntimeInstanceController(StoryRuntimeInstanceService service) {
        this.service = service;
    }

    @PostMapping("/start")
    public ResponseEntity<?> start(@RequestBody StoryStartRequest request, Authentication auth) {
        try {
            return ResponseEntity.ok(service.startOrReuseStory(auth.getName(), request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{instanceKey}")
    public ResponseEntity<?> get(@PathVariable String instanceKey, Authentication auth) {
        try {
            return ResponseEntity.ok(service.getStory(auth.getName(), instanceKey));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/choice")
    public ResponseEntity<?> choice(@RequestBody StoryDecisionRequest request, Authentication auth) {
        try {
            return ResponseEntity.ok(service.submitChoice(auth.getName(), request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }
}
