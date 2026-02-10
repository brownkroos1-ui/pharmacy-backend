CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT,
    message TEXT,
    created_at DATETIME NOT NULL
);

CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
CREATE INDEX idx_audit_logs_actor ON audit_logs (actor);
CREATE INDEX idx_audit_logs_entity ON audit_logs (entity_type, entity_id);
