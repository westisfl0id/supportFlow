package com.supportflow.ticket.attachment.repository;

import com.supportflow.ticket.attachment.entity.TicketAttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketAttachmentRepository extends JpaRepository<TicketAttachmentEntity, Long> {
    List<TicketAttachmentEntity> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
}
