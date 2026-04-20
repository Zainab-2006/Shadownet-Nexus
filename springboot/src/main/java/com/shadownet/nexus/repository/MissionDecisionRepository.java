package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.MissionDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MissionDecisionRepository extends JpaRepository<MissionDecision, Long> {
    List<MissionDecision> findByMissionInstanceId(Long missionInstanceId);
}
