package com.shadownet.nexus.service;

import com.shadownet.nexus.entity.UserSkill;
import com.shadownet.nexus.repository.UserSkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;

@Service
public class AdaptiveEngineService {

    @Autowired
    private UserSkillRepository skillRepository;

    @Transactional
    public void updateSkill(String userId, String category, boolean solved, long timeSpentSeconds) {
        UserSkill skill = skillRepository.getOrCreateSkill(userId, category);

        int delta = solved ? 15 : -3; // Solved +15 XP, failed -3 XP

        // Time bonus: fast solve = more XP
        if (solved && timeSpentSeconds < 900) { // Under 15 min
            delta += 5 + (int) (900 - timeSpentSeconds) / 60; // 1 XP per minute under 15
        }

        skill.setXp(Math.max(0, skill.getXp() + delta));
        skillRepository.save(skill);
    }

    public List<UserSkill> getUserSkills(String userId) {
        return skillRepository.findByUserId(userId);
    }

    public List<UserSkill> getTopSkills(String userId) {
        return skillRepository.findTopSkillsByUserId(userId);
    }

    /**
     * Recommend challenges matching user skill level
     * skill < 30 → easy, 30-60 → medium, >60 → hard/insane
     */
    public List<String> getRecommendedCategories(String userId) {
        List<UserSkill> skills = getUserSkills(userId);
        // Implementation: return categories where skill matches difficulty
        return skills.stream()
                .filter(s -> s.getLevel() < 4) // Recommend based on lower skills
                .map(UserSkill::getCategory)
                .toList();
    }
}
