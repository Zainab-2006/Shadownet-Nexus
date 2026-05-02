package com.shadownet.nexus.service;

import com.shadownet.nexus.entity.TrustEntity;
import com.shadownet.nexus.entity.User;
import com.shadownet.nexus.repository.TrustRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@Transactional
public class TrustService {

    @Autowired
    private TrustRepository trustRepository;

    public void updateTrust(User user, String targetEntity, Integer delta) {
        updateTrustScore(user.getId(), targetEntity, delta);
    }

    public int getTrustLevel(User user, String targetEntity) {
        return getOrCreateTrust(user.getId(), targetEntity).getTrustScore();
    }

    public TrustEntity getOrCreateTrust(String userId, String targetUserId) {
        return trustRepository.findByUserIdAndTargetUserId(userId, targetUserId)
                .orElseGet(() -> {
                    TrustEntity newTrust = new TrustEntity();
                    newTrust.setUserId(userId);
                    newTrust.setTargetUserId(targetUserId);
                    newTrust.setTrustScore(0);
                    newTrust.setUpdatedAt(LocalDateTime.now());
                    return trustRepository.save(newTrust);
                });
    }

    public TrustEntity updateTrustScore(String userId, String targetUserId, int delta) {
        TrustEntity trust = getOrCreateTrust(userId, targetUserId);
        int newScore = Math.max(-100, Math.min(100, trust.getTrustScore() + delta));
        trust.setTrustScore(newScore);
        trust.setUpdatedAt(LocalDateTime.now());
        return trustRepository.save(trust);
    }

    public int calculateTrust(String userId, String targetUserId, long seed) {
        Random random = new Random(seed);
        int baseTrust = random.nextInt(41) - 20; // -20 to +20 base
        TrustEntity trust = getOrCreateTrust(userId, targetUserId);
        return Math.max(-100, Math.min(100, trust.getTrustScore() + baseTrust));
    }

    public boolean accuseUser(String userId, String targetUserId) {
        TrustEntity trust = getOrCreateTrust(userId, targetUserId);
        if (trust.getTrustScore() >= -50) {
            // Accusation rejected - low trust required
            return false;
        }
        // Accusation successful - update trust (team fallout)
        trust.setTrustScore(trust.getTrustScore() - 30);
        trustRepository.save(trust);
        return true;
    }
}
