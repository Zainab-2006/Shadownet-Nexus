package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.StoryEndingDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoryEndingDefinitionRepository extends JpaRepository<StoryEndingDefinition, Long> {
    Optional<StoryEndingDefinition> findByEndingKey(String endingKey);
    List<StoryEndingDefinition> findAllByOrderByUnlockRankAscMinimumTrustDesc();
}
