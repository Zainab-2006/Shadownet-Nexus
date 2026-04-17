package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.User;
import com.shadownet.nexus.entity.UserMissionState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserMissionStateRepository extends JpaRepository<UserMissionState, Long> {
    Optional<UserMissionState> findByUserAndMissionId(User user, String missionId);

    List<UserMissionState> findByUserOrderByUpdatedAtDesc(User user);
}
