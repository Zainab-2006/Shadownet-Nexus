package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.MissionInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MissionInstanceRepository extends JpaRepository<MissionInstance, Long> {
    Optional<MissionInstance> findByInstanceKey(String instanceKey);
    Optional<MissionInstance> findFirstByOwnerUserIdAndMissionCodeAndStatus(String ownerUserId, String missionCode, String status);
    List<MissionInstance> findByOwnerUserId(String ownerUserId);
}
