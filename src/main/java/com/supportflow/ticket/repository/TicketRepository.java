package com.supportflow.ticket.repository;

import com.supportflow.ticket.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<TicketEntity, Long> {
    List<TicketEntity> findByCreatedById(Long userId);

    List<TicketEntity> findByAssignedToId(Long agentId);
}
