package com.shadownet.nexus.service;

import com.shadownet.nexus.entity.User;
import com.shadownet.nexus.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ScoreAwardService {

    private final UserRepository userRepository;

    public ScoreAwardService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void awardSoloPCGPoints(String userId, Integer points) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || points == null || points <= 0) {
            return;
        }

        int nextScore = (user.getScore() != null ? user.getScore() : 0) + points;
        int nextXp = (user.getXp() != null ? user.getXp() : 0) + points;
        user.setScore(nextScore);
        user.setXp(nextXp);
        user.setLevel(Math.max(1, (nextXp / 500) + 1));
        user.setUpdatedAt(System.currentTimeMillis());
        userRepository.save(user);
    }
}
