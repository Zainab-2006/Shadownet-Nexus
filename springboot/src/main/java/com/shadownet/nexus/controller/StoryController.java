package com.shadownet.nexus.controller;

import com.shadownet.nexus.dto.*;
import com.shadownet.nexus.service.StoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/story")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed.origins:*}")
public class StoryController {

    private final StoryService storyService;

    @GetMapping("/chapters")
    public ResponseEntity<List<ChapterDTO>> getChapters(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(storyService.getChapters(userDetails.getUsername()));
    }

    @GetMapping("/chapters/{id}")
    public ResponseEntity<ChapterDTO> getChapter(@PathVariable Long id) {
        return ResponseEntity.ok(storyService.getChapter(id));
    }

    @GetMapping("/chapters/{id}/first-scene")
    public ResponseEntity<SceneDTO> getFirstScene(@PathVariable Long id) {
        return ResponseEntity.ok(storyService.getFirstScene(id));
    }

    @GetMapping("/scenes/{id}")
    public ResponseEntity<SceneDTO> getScene(@PathVariable Long id) {
        return ResponseEntity.ok(storyService.getScene(id));
    }

    @GetMapping("/progress")
    public ResponseEntity<StoryProgressDTO> getProgress(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(storyService.getProgress(userDetails.getUsername()));
    }

    @GetMapping("/chapters/{id}/debrief")
    public ResponseEntity<ChapterDebriefDTO> getChapterDebrief(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(storyService.getChapterDebrief(id, userDetails.getUsername()));
    }

    @PostMapping("/save")
    public ResponseEntity<StoryProgressDTO> saveProgress(@RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(storyService.saveProgressSnapshot(payload, userDetails.getUsername()));
    }

    @PostMapping("/decision")
    public ResponseEntity<?> makeDecision(
            @Valid @RequestBody DecisionRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(storyService.makeDecision(request, userDetails.getUsername()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of(
                    "error", "STORY_DECISION_CONFLICT",
                    "message", e.getMessage()));
        }
    }

    @PostMapping("/progress/reset")
    public ResponseEntity<Void> resetProgress(@AuthenticationPrincipal UserDetails userDetails) {
        storyService.resetProgress(userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
