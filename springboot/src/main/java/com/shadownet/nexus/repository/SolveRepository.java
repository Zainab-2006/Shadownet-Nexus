package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.Solve;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolveRepository extends JpaRepository<Solve, Long> {

    int countByChallengeId(String challengeId);

    List<Solve> findByUserId(String userId);

    boolean existsByUserIdAndChallengeId(String userId, String challengeId);

}