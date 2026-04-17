package com.shadownet.nexus.controller;

import com.shadownet.nexus.dto.ErrorResponse;
import com.shadownet.nexus.dto.OperatorDto;
import com.shadownet.nexus.entity.Operator;
import com.shadownet.nexus.entity.Solve;
import com.shadownet.nexus.entity.StoryProgress;
import com.shadownet.nexus.entity.User;
import com.shadownet.nexus.repository.OperatorRepository;
import com.shadownet.nexus.repository.SolveRepository;
import com.shadownet.nexus.repository.StoryChapterRepository;
import com.shadownet.nexus.repository.StoryProgressRepository;
import com.shadownet.nexus.repository.UserRepository;
import com.shadownet.nexus.util.AuthenticationAuditLogger;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SolveRepository solveRepository;

    @Autowired
    private StoryProgressRepository storyProgressRepository;

    @Autowired
    private StoryChapterRepository storyChapterRepository;

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private AuthenticationAuditLogger auditLogger;

    @GetMapping({ "/user", "/users/me" })
    public ResponseEntity<?> getUser(Authentication auth, HttpServletRequest request) {
        try {
            User user = requireUser(auth);
            return ResponseEntity.ok(new UserProfile(user));
        } catch (Exception e) {
            logger.error("Error retrieving user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Unable to retrieve user profile", 500));
        }
    }

    @GetMapping("/users/me/progress")
    public ResponseEntity<?> getUserProgress(Authentication auth) {
        try {
            User user = requireUser(auth);
            List<Solve> solves = solveRepository.findByUserId(user.getId());
            StoryProgress storyProgress = storyProgressRepository.findByUser(user).orElse(null);
            int totalChapters = (int) storyChapterRepository.count();
            int completedChapters = storyProgress != null ? storyProgress.getCompletedChapters().size() : 0;
            int storyPercent = totalChapters > 0 ? (completedChapters * 100) / totalChapters : 0;

            UserProgressProfile profile = new UserProgressProfile();
            profile.userId = user.getId();
            profile.totalXp = user.getXp() != null ? user.getXp() : 0;
            profile.currentLevel = user.getLevel() != null ? user.getLevel() : 1;
            profile.totalPoints = user.getScore() != null ? user.getScore() : 0;
            profile.rankPoints = profile.totalPoints;
            profile.challengesSolved = solves.size();
            profile.missionsCompleted = 0;
            profile.storyProgressPercent = storyPercent;
            profile.solvedChallengeIds = solves.stream().map(Solve::getChallengeId).distinct().toList();
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            logger.error("Error retrieving user progression: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Unable to retrieve user progression", 500));
        }
    }

    @GetMapping("/users/me/operator")
    public ResponseEntity<?> getSelectedOperator(Authentication auth) {
        try {
            User user = requireUser(auth);
            if (user.getSelectedOperator() == null || user.getSelectedOperator().isBlank()) {
                return ResponseEntity.ok(Map.of("selectedOperator", (Object) null));
            }

            Operator operator = operatorRepository.findById(user.getSelectedOperator()).orElse(null);
            if (operator == null) {
                return ResponseEntity.ok(Map.of("selectedOperator", user.getSelectedOperator()));
            }

            OperatorDto dto = new OperatorDto(
                    operator.getId(),
                    operator.getName(),
                    operator.getRole(),
                    operator.getAbilities(),
                    operator.getUnlockCost(),
                    operator.getBackstory(),
                    true,
                    true,
                    "/images/operators/" + operator.getId().toLowerCase() + ".png",
                    "/images/operators/" + operator.getId().toLowerCase() + ".png");
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            logger.error("Error retrieving selected operator: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Unable to retrieve selected operator", 500));
        }
    }

    @GetMapping({ "/user/story-progress", "/users/me/story-progress" })
    public ResponseEntity<?> getStoryProgress(Authentication auth) {
        try {
            User user = requireUser(auth);
            String storyProgress = user.getStoryProgress();
            if (storyProgress == null || storyProgress.isBlank()) {
                return ResponseEntity.ok(Map.of("storyProgress", Map.of()));
            }
            return ResponseEntity.ok(Map.of("storyProgress", storyProgress));
        } catch (Exception e) {
            logger.error("Error retrieving story progress: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Unable to fetch story progress", 500));
        }
    }

    @PutMapping({ "/user/story-progress", "/users/me/story-progress" })
    public ResponseEntity<?> setStoryProgress(@RequestBody Map<String, Object> payload, Authentication auth) {
        try {
            User user = requireUser(auth);
            Object progress = payload.get("storyProgress");
            if (progress == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "storyProgress is required"));
            }

            user.setStoryProgress(progress.toString());
            user.setUpdatedAt(System.currentTimeMillis());
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("storyProgress", user.getStoryProgress()));
        } catch (Exception e) {
            logger.error("Error updating story progress: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Unable to update story progress", 500));
        }
    }

    private User requireUser(Authentication auth) {
        String userId = auth.getName();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IllegalStateException("User not found");
        }
        return user;
    }

    public static class UserProfile {
        private String id;
        private String username;
        private String displayName;
        private String email;
        private Integer score;
        private Integer xp;
        private Integer level;
        private String selectedOperator;
        private Long createdAt;
        private Long lastLoginAt;

        public UserProfile(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.displayName = user.getDisplayName();
            this.email = user.getEmail();
            this.score = user.getScore() != null ? user.getScore() : 0;
            this.xp = user.getXp() != null ? user.getXp() : 0;
            this.level = user.getLevel() != null ? user.getLevel() : 1;
            this.selectedOperator = user.getSelectedOperator();
            this.createdAt = user.getCreatedAt();
            this.lastLoginAt = user.getLastLoginAt();
        }

        public String getId() { return id; }
        public String getUsername() { return username; }
        public String getDisplayName() { return displayName; }
        public String getEmail() { return email; }
        public Integer getScore() { return score; }
        public Integer getXp() { return xp; }
        public Integer getLevel() { return level; }
        public String getSelectedOperator() { return selectedOperator; }
        public Long getCreatedAt() { return createdAt; }
        public Long getLastLoginAt() { return lastLoginAt; }
    }

    public static class UserProgressProfile {
        public String userId;
        public int totalXp;
        public int currentLevel;
        public int totalPoints;
        public int rankPoints;
        public int challengesSolved;
        public int missionsCompleted;
        public int storyProgressPercent;
        public List<String> solvedChallengeIds;
    }
}
