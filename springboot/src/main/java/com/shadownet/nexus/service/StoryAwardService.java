package com.shadownet.nexus.service;

import com.shadownet.nexus.entity.User;
import com.shadownet.nexus.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class StoryAwardService {
    private final UserRepository userRepository;
    private final EventService eventService;

    public StoryAwardService(UserRepository userRepository, EventService eventService) {
        this.userRepository = userRepository;
        this.eventService = eventService;
    }

    public void awardStoryProgress(String userId, String instanceKey, int xp) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || xp <= 0) {
            return;
        }
        int nextXp = (user.getXp() == null ? 0 : user.getXp()) + xp;
        user.setXp(nextXp);
        user.setLevel(Math.max(1, (nextXp / 500) + 1));
        user.setUpdatedAt(System.currentTimeMillis());
        userRepository.save(user);
        eventService.logEvent(userId, "story_runtime_award", instanceKey, "story", java.util.Map.of("xp", xp));
    }

    public void recordStoryChoice(String userId, String instanceKey, String choiceKey) {
        eventService.logEvent(userId, "story_runtime_choice", instanceKey, "story", java.util.Map.of("choiceKey", choiceKey));
    }
}
