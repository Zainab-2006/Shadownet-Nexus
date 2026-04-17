package com.shadownet.nexus.aspect;

import com.shadownet.nexus.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditableAspect {
    
    private final AuditService auditService;
    
    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void logSuccess(JoinPoint joinPoint, Auditable auditable, Object result) {
        String username = getCurrentUsername();
        HttpServletRequest request = getCurrentRequest();
        
        auditService.log(
            username,
            auditable.action(),
            auditable.entityType(),
            extractEntityId(joinPoint),
            null,
            request,
            true
        );
    }
    
    @AfterThrowing(pointcut = "@annotation(auditable)", throwing = "exception")
    public void logFailure(JoinPoint joinPoint, Auditable auditable, Exception exception) {
        String username = getCurrentUsername();
        HttpServletRequest request = getCurrentRequest();
        
        Map<String, Object> details = new HashMap<>();
        details.put("error", exception.getMessage());
        
        auditService.log(
            username,
            auditable.action(),
            auditable.entityType(),
            extractEntityId(joinPoint),
            details,
            request,
            false
        );
    }
    
    private String getCurrentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }
    
    private HttpServletRequest getCurrentRequest() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) attributes).getRequest();
        }
        return null;
    }
    
    private Long extractEntityId(JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Number) {
                return ((Number) arg).longValue();
            }
            if (arg instanceof String && ((String) arg).matches("\\d+")) {
                return Long.parseLong((String) arg);
            }
        }
        return null;
    }
}

