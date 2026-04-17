package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.ChallengeStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChallengeStageRepository extends JpaRepository<ChallengeStage, String> {
  List<ChallengeStage> findByChallengeIdOrderByStageNumberAsc(String challengeId);
}
