package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.MissionEvidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MissionEvidenceRepository extends JpaRepository<MissionEvidence, Long> {
    List<MissionEvidence> findByMissionInstanceId(Long missionInstanceId);
    Optional<MissionEvidence> findByMissionInstanceIdAndEvidenceKey(Long missionInstanceId, String evidenceKey);
}
