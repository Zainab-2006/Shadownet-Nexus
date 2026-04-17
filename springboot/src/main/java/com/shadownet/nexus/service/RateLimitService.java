package com.shadownet.nexus.service;

import com.shadownet.nexus.entity.RateLimitRecord;
import com.shadownet.nexus.repository.RateLimitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import com.shadownet.nexus.security.RateLimitingFilter;

@Service
@RequiredArgsConstructor
public class RateLimitService {
    
    private final RateLimitRepository rateLimitRepository;
    
    @Transactional
    public boolean isAllowed(String key, RateLimitingFilter.RateLimitType type) {
        String recordKey = key + ":" + type.name();
        
        RateLimitRecord record = rateLimitRepository.findByKeyName(recordKey).orElse(null);
        
        LocalDateTime now = LocalDateTime.now();
        
        if (record == null) {
            record = RateLimitRecord.builder()
                .keyName(recordKey)
                .actionType(type.name())
                .attemptCount(1)
                .firstAttempt(now)
                .lastAttempt(now)
                .blockedUntil(null)
                .build();
            rateLimitRepository.save(record);
            return true;
        }
        
        if (record.getBlockedUntil() != null && record.getBlockedUntil().isAfter(now)) {
            return false;
        }
        
        if (record.getFirstAttempt().plusSeconds(type.windowSeconds).isBefore(now)) {
            record.setAttemptCount(1);
            record.setFirstAttempt(now);
            record.setLastAttempt(now);
            record.setBlockedUntil(null);
            rateLimitRepository.save(record);
            return true;
        }
        
        if (record.getAttemptCount() >= type.maxAttempts) {
            record.setBlockedUntil(now.plusMinutes(5));
            record.setLastAttempt(now);
            rateLimitRepository.save(record);
            return false;
        }
        
        record.setAttemptCount(record.getAttemptCount() + 1);
        record.setLastAttempt(now);
        rateLimitRepository.save(record);
        return true;
    }
}

