package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.StoryInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoryInstanceRepository extends JpaRepository<StoryInstance, Long> {
    Optional<StoryInstance> findByInstanceKey(String instanceKey);
    Optional<StoryInstance> findFirstByUserIdAndOperatorCodeAndStatus(String userId, String operatorCode, String status);
    List<StoryInstance> findByUserId(String userId);
}
