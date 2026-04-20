package com.shadownet.nexus.service;

import com.shadownet.nexus.entity.User;
import com.shadownet.nexus.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class MissionAwardService {
    private final UserRepository userRepository;
    private final EventService eventService;

    public MissionAwardService(UserRepository userRepository, EventService eventService) {
        this.userRepository = userRepository;
        this.eventService = eventService;
    }

    public void awardMissionCompletion(String userId, String missionInstanceKey, int credits, int xp) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }
        int award = Math.max(0, credits) + Math.max(0, xp);
        int nextScore = (user.getScore() == null ? 0 : user.getScore()) + award;
        int nextXp = (user.getXp() == null ? 0 : user.getXp()) + Math.max(0, xp);
        user.setScore(nextScore);
        user.setXp(nextXp);
        user.setLevel(Math.max(1, (nextXp / 500) + 1));
        user.setUpdatedAt(System.currentTimeMillis());
        userRepository.save(user);
        eventService.logEvent(userId, "mission_runtime_completed", missionInstanceKey, "mission", java.util.Map.of(
                "credits", credits,
                "xp", xp,
                "award", award));
    }
}
