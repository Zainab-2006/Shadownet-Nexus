package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.TrustEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface TrustRepository extends JpaRepository<TrustEntity, Long> {

    Optional<TrustEntity> findByUserIdAndTargetUserId(String userId, String targetUserId);

    @Query("SELECT t FROM TrustEntity t WHERE t.userId = :userId AND t.targetUserId = :targetId")
    Optional<TrustEntity> findTrustRelationship(@Param("userId") String userId, @Param("targetId") String targetId);

    boolean existsByUserIdAndTargetUserId(String userId, String targetUserId);
}
