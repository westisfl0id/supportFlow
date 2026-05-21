package com.supportflow.audit.repository;

import com.supportflow.audit.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
    List<AuditLogEntity> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
