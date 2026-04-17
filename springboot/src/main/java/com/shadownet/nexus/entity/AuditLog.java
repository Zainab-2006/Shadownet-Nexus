package com.shadownet.nexus.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;

    @Column(length = 100)
    private String entityType;

    private Long entityId;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(columnDefinition = "TINYINT")
    private boolean success;

    private String userAgent;

    @Column(name = "username")
    private String username;

    private String ipAddress;

    private String event;
    private String userId;
    private String ip;
    private Long timestamp;

    @Column(columnDefinition = "JSON")
    private String details;

    private LocalDateTime createdAt;
}