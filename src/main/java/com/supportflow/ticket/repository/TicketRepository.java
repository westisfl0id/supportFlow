package com.supportflow.ticket.repository;

import com.supportflow.ticket.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface TicketRepository extends JpaRepository<TicketEntity, Long>, JpaSpecificationExecutor<TicketEntity> {
    List<TicketEntity> findByCreatedById(Long userId);

    List<TicketEntity> findByAssignedToId(Long agentId);
}
