package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.PuzzleSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;

@Repository
public interface PuzzleSessionRepository extends JpaRepository<PuzzleSession, String> {

    // Find active session for user + challenge
    @Query("SELECT s FROM PuzzleSession s WHERE s.userId = :userId AND s.challengeId = :challengeId AND s.completed = false")
    Optional<PuzzleSession> findActiveByUserIdAndChallengeId(@Param("userId") String userId,
            @Param("challengeId") String challengeId);

    // Find all active sessions for user
    @Query("SELECT s FROM PuzzleSession s WHERE s.userId = :userId AND s.completed = false")
    List<PuzzleSession> findActiveByUserId(@Param("userId") String userId);

    // Update hints used and touch updated_at
    @Transactional
    @Modifying
    @Query("UPDATE PuzzleSession s SET s.hintsUsed = :hintsUsed, s.updatedAt = :now WHERE s.id = :id")
    int incrementHintsUsed(@Param("id") String id, @Param("hintsUsed") int hintsUsed, @Param("now") Long now);

    // Update stage and touch
    @Transactional
    @Modifying
    @Query("UPDATE PuzzleSession s SET s.currentStage = :stage, s.updatedAt = :now WHERE s.id = :id")
    int advanceStage(@Param("id") String id, @Param("stage") int stage, @Param("now") Long now);

    // Mark as completed
    @Transactional
    @Modifying
    @Query("UPDATE PuzzleSession s SET s.completed = true, s.updatedAt = :now WHERE s.id = :id")
    int completeSession(@Param("id") String id, @Param("now") Long now);

    // Cleanup old sessions (optional cron job)
    @Transactional
    @Modifying
    @Query("DELETE FROM PuzzleSession s WHERE s.updatedAt < :cutoff")
    int deleteInactiveSessions(@Param("cutoff") Long cutoff);
}
