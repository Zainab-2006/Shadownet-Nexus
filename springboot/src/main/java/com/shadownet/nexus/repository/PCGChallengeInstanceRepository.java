package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.PCGChallengeInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PCGChallengeInstanceRepository extends JpaRepository<PCGChallengeInstance, Long> {
    Optional<PCGChallengeInstance> findByInstanceKey(String instanceKey);
    List<PCGChallengeInstance> findByUserIdAndStatus(String userId, String status);
    Optional<PCGChallengeInstance> findFirstByUserIdAndSessionIdAndStatus(String userId, String sessionId, String status);
}
