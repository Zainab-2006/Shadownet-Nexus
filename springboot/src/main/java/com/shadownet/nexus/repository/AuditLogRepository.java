package com.shadownet.nexus.repository;

import com.shadownet.nexus.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDateTime;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

List<AuditLog> findByUsername(String username);

    List<AuditLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByActionAndCreatedAtAfter(String action, LocalDateTime since);

    List<AuditLog> findByUsernameAndAction(String username, String action);

}