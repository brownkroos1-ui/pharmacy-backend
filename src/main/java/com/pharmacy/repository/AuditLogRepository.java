package com.pharmacy.repository;

import com.pharmacy.model.AuditLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
        SELECT a FROM AuditLog a
        WHERE (:actor IS NULL OR :actor = '' OR LOWER(a.actor) LIKE LOWER(CONCAT('%', :actor, '%')))
          AND (:entityType IS NULL OR :entityType = '' OR a.entityType = :entityType)
          AND (:action IS NULL OR :action = '' OR a.action = :action)
        ORDER BY a.createdAt DESC
    """)
    List<AuditLog> search(
            @Param("actor") String actor,
            @Param("entityType") String entityType,
            @Param("action") String action,
            Pageable pageable
    );
}
