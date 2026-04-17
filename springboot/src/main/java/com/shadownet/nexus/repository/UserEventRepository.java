package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.UserEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserEventRepository extends JpaRepository<UserEvent, String> {

    // Recent events for user (last hour)
    List<UserEvent> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
            String userId, Long timestamp, Pageable pageable);

    // Fail rate analysis
    @Query("SELECT e FROM UserEvent e WHERE e.userId = :userId AND e.eventType = :eventType ORDER BY e.createdAt DESC")
    List<UserEvent> findRecentFailsByUserId(@Param("userId") String userId, @Param("eventType") String eventType);

    @Query("SELECT COUNT(e) FROM UserEvent e WHERE e.userId = :userId AND e.eventType = 'flag_submit_wrong' AND e.createdAt > :cutoff")
    long countRecentFails(@Param("userId") String userId, @Param("cutoff") Long cutoff);

    @Query("SELECT COUNT(e) FROM UserEvent e WHERE e.userId = :userId AND e.eventType IN ('flag_submit_correct', 'flag_submit_wrong') AND e.createdAt > :cutoff")
    long countRecentAttempts(@Param("userId") String userId, @Param("cutoff") Long cutoff);

    // Risk-level shortcuts
    default boolean isHighRisk(String userId) {
        Long cutoff = System.currentTimeMillis() - 3600000; // 1 hour
        return countRecentFails(userId, cutoff) > 7;
    }

    default boolean isMediumRisk(String userId) {
        Long cutoff = System.currentTimeMillis() - 3600000; // 1 hour
        return countRecentFails(userId, cutoff) > 3;
    }

    // Category-specific fail streaks
    @Query("SELECT e.category, COUNT(e) as failCount FROM UserEvent e " +
            "WHERE e.userId = :userId AND e.eventType = 'flag_submit_wrong' " +
            "GROUP BY e.category ORDER BY failCount DESC")
    List<Object[]> getCategoryFailStreaks(@Param("userId") String userId);
}
