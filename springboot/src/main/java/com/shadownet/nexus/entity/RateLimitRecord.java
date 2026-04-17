package com.shadownet.nexus.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rate_limit_records", indexes = {
    @Index(name = "idx_key_name", columnList = "keyName")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RateLimitRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 255)
    private String keyName;
    
    @Column(nullable = false, length = 50)
    private String actionType;
    
    @Column(nullable = false)
    private int attemptCount = 1;
    
    @Column(name = "first_attempt")
    private LocalDateTime firstAttempt;
    
    @Column(name = "last_attempt")
    private LocalDateTime lastAttempt;
    
    @Column(name = "blocked_until")
    private LocalDateTime blockedUntil;
}

