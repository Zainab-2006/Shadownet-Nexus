package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.RateLimitRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RateLimitRepository extends JpaRepository<RateLimitRecord, Long> {
    
    Optional<RateLimitRecord> findByKeyName(String keyName);
}

