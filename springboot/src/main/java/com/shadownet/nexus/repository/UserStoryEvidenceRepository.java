package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.User;
import com.shadownet.nexus.entity.UserStoryEvidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserStoryEvidenceRepository extends JpaRepository<UserStoryEvidence, Long> {
    Optional<UserStoryEvidence> findByUserAndEvidenceCode(User user, String evidenceCode);

    List<UserStoryEvidence> findByUserOrderByDiscoveredAtDesc(User user);
}
