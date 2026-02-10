package com.pharmacy.service;

import com.pharmacy.model.AuditLog;
import com.pharmacy.repository.AuditLogRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String action, String entityType, Long entityId, String message) {
        log(action, entityType, entityId, message, null);
    }

    public void log(String action,
                    String entityType,
                    Long entityId,
                    String message,
                    String actorOverride) {
        try {
            AuditLog log = new AuditLog();
            log.setActor(resolveActor(actorOverride));
            log.setAction(action);
            log.setEntityType(entityType);
            log.setEntityId(entityId);
            log.setMessage(message);
            auditLogRepository.save(log);
        } catch (Exception ex) {
            // Avoid breaking core flows if audit logging fails
            System.err.println("Audit log failure: " + ex.getMessage());
        }
    }

    private String resolveActor(String actorOverride) {
        if (actorOverride != null && !actorOverride.isBlank()) {
            return actorOverride;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
            return auth.getName();
        }
        return "system";
    }
}
