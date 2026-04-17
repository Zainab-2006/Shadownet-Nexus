package com.shadownet.nexus.service;

import com.shadownet.nexus.entity.AuditLog;
import com.shadownet.nexus.entity.User;
import com.shadownet.nexus.repository.AuditLogRepository;
import com.shadownet.nexus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public void log(String username, String action, String entityType, Long entityId,
            Map<String, Object> details, HttpServletRequest request, boolean success) {

        AuditLog log = new AuditLog();

        if (username != null) {
            User user = userRepository.findByUsername(username);
            if (user != null) {
                log.setUsername(user.getUsername());
                log.setUserId(user.getId()); // Legacy field
            }
        }

        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details != null ? details.toString() : null); // JSON via @Column
        log.setSuccess(success);

        if (request != null) {
            log.setIpAddress(getClientIp(request));
            log.setUserAgent(request.getHeader("User-Agent"));
        }

        log.setCreatedAt(LocalDateTime.now());

        auditLogRepository.save(log);
    }

    public void log(String username, String action, String entityType, Long entityId, boolean success) {
        log(username, action, entityType, entityId, null, null, success);
    }

    public void log(String username, String action, boolean success) {
        log(username, action, null, null, null, null, success);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
