package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.MissionSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MissionSessionRepository extends JpaRepository<MissionSession, String> {
    Optional<MissionSession> findFirstByUserIdAndMissionIdOrderByStartedAtDesc(String userId, String missionId);
}