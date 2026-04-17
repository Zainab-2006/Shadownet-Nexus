package com.shadownet.nexus.service;

import com.shadownet.nexus.entity.RateLimitRecord;
import com.shadownet.nexus.repository.RateLimitRepository;
import com.shadownet.nexus.security.RateLimitingFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {
    
    @Mock
    private RateLimitRepository rateLimitRepository;
    
    @InjectMocks
    private RateLimitService rateLimitService;
    
    @Test
    void isAllowed_FirstAttempt_ShouldReturnTrue() {
        when(rateLimitRepository.findByKeyName(any())).thenReturn(Optional.empty());
        
        boolean allowed = rateLimitService.isAllowed("test-key", RateLimitingFilter.RateLimitType.AUTH);
        
        assertThat(allowed).isTrue();
        verify(rateLimitRepository).save(any());
    }
    
    @Test
    void isAllowed_WithinLimit_ShouldReturnTrue() {
        RateLimitRecord record = new RateLimitRecord();
        record.setAttemptCount(3);
        record.setFirstAttempt(LocalDateTime.now());
        
        when(rateLimitRepository.findByKeyName(any())).thenReturn(Optional.of(record));
        
        boolean allowed = rateLimitService.isAllowed("test-key", RateLimitingFilter.RateLimitType.AUTH);
        
        assertThat(allowed).isTrue();
        verify(rateLimitRepository).save(record);
    }
    
    @Test
    void isAllowed_ExceedsLimit_ShouldReturnFalse() {
        RateLimitRecord record = new RateLimitRecord();
        record.setAttemptCount(5);
        record.setFirstAttempt(LocalDateTime.now());
        
        when(rateLimitRepository.findByKeyName(any())).thenReturn(Optional.of(record));
        
        boolean allowed = rateLimitService.isAllowed("test-key", RateLimitingFilter.RateLimitType.AUTH);
        
        assertThat(allowed).isFalse();
        assertThat(record.getBlockedUntil()).isNotNull();
        verify(rateLimitRepository).save(record);
    }
}
