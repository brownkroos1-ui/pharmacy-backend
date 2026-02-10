package com.pharmacy.controller;

import com.pharmacy.model.AuditLog;
import com.pharmacy.repository.AuditLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/audit")
public class AdminAuditController {

    private final AuditLogRepository auditLogRepository;

    public AdminAuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLog> list(
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "100") int limit
    ) {
        int safeLimit = Math.min(Math.max(limit, 1), 500);
        return auditLogRepository.search(
                actor,
                entityType,
                action,
                PageRequest.of(0, safeLimit)
        );
    }
}
