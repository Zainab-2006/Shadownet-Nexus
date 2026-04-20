package com.shadownet.nexus.controller;

import com.shadownet.nexus.entity.Challenge;
import com.shadownet.nexus.entity.UserSkill;
import com.shadownet.nexus.mapper.ChallengeViewMapper;
import com.shadownet.nexus.repository.ChallengeRepository;
import com.shadownet.nexus.repository.UserSkillRepository;
import com.shadownet.nexus.service.AdaptiveEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class RecommendedController {

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private UserSkillRepository skillRepository;

    @Autowired
    private AdaptiveEngineService adaptiveService;

    @Autowired
    private ChallengeViewMapper challengeViewMapper;

    @GetMapping("/challenges/recommended")
    public ResponseEntity<?> getRecommendedChallenges(Authentication auth,
            @RequestParam(required = false) String userId) {
        String currentUserId = userId != null ? userId : auth.getName();

        // Get user's top under-developed skills
        List<UserSkill> skills = skillRepository.findTopSkillsByUserId(currentUserId);

        // Recommend challenges matching skill gaps
        List<Challenge> allChallenges = challengeRepository.findAll();
        List<Challenge> recommended = allChallenges.stream()
                .filter(ch -> {
                    // Match category to skill gap (level < 4 = needs work)
                    return skills.stream().anyMatch(s -> s.getCategory().equals(ch.getCategory()) && s.getLevel() < 4);
                })
                .limit(6)
                .collect(Collectors.toList());

        return ResponseEntity.ok(recommended.stream()
                .map(challengeViewMapper::toListDto)
                .toList());
    }
}
