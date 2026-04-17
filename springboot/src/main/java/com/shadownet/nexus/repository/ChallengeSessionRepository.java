package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.ChallengeSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ChallengeSessionRepository extends JpaRepository<ChallengeSession, String> {
    Optional<ChallengeSession> findByUserIdAndChallengeId(String userId, String challengeId);
}
