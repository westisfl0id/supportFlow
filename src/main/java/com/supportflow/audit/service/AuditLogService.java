package com.supportflow.audit.service;

import com.supportflow.audit.dto.AuditLogResponse;
import com.supportflow.audit.entity.AuditLogEntity;
import com.supportflow.audit.enums.AuditAction;
import com.supportflow.audit.repository.AuditLogRepository;
import com.supportflow.security.CurrentUserService;
import com.supportflow.ticket.entity.TicketEntity;
import com.supportflow.ticket.enums.TicketStatus;
import com.supportflow.ticket.exception.TicketNotFoundException;
import com.supportflow.ticket.repository.TicketRepository;
import com.supportflow.ticket.service.TicketAccessService;
import com.supportflow.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final TicketRepository ticketRepository;
    private final CurrentUserService currentUserService;
    private final TicketAccessService ticketAccessService;

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getTicketTimeline(Long ticketId) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        UserEntity currentUser = currentUserService.getCurrentUser();

        ticketAccessService.checkCanViewTicket(currentUser, ticket);

        return auditLogRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional
    public void logTicketCreated(TicketEntity ticket, UserEntity actor) {
        save(
                ticket,
                actor,
                AuditAction.TICKET_CREATED,
                null,
                ticket.getStatus().name(),
                "Обращение создано"
        );
    }

    @Transactional
    public void logStatusChanged(
            TicketEntity ticket,
            UserEntity actor,
            TicketStatus oldStatus,
            TicketStatus newStatus
    ) {
        save(
                ticket,
                actor,
                AuditAction.STATUS_CHANGED,
                oldStatus.name(),
                newStatus.name(),
                "Статус обращения изменён с " + oldStatus + " на " + newStatus
        );
    }

    @Transactional
    public void logTicketAssigned(
            TicketEntity ticket,
            UserEntity actor,
            UserEntity oldAgent,
            UserEntity newAgent
    ) {
        save(
                ticket,
                actor,
                AuditAction.TICKET_ASSIGNED,
                userToString(oldAgent),
                userToString(newAgent),
                "Обращение назначено исполнителю: " + userToString(newAgent)
        );
    }

    @Transactional
    public void logCommentAdded(TicketEntity ticket, UserEntity actor) {
        save(
                ticket,
                actor,
                AuditAction.COMMENT_ADDED,
                null,
                null,
                "Добавлен комментарий к обращению"
        );
    }

    @Transactional
    public void logAttachmentUploaded(TicketEntity ticket, UserEntity actor, String filename) {
        save(
                ticket,
                actor,
                AuditAction.ATTACHMENT_UPLOADED,
                null,
                filename,
                "Загружено вложение: " + filename
        );
    }

    @Transactional
    public void logTicketResolved(TicketEntity ticket, UserEntity actor, TicketStatus oldStatus) {
        save(
                ticket,
                actor,
                AuditAction.TICKET_RESOLVED,
                oldStatus.name(),
                TicketStatus.RESOLVED.name(),
                "Обращение переведено в статус RESOLVED"
        );
    }

    @Transactional
    public void logTicketReopened(TicketEntity ticket, UserEntity actor, TicketStatus oldStatus) {
        save(
                ticket,
                actor,
                AuditAction.TICKET_REOPENED,
                oldStatus.name(),
                TicketStatus.IN_PROGRESS.name(),
                "Обращение переоткрыто"
        );
    }

    @Transactional
    public void logTicketClosed(TicketEntity ticket, UserEntity actor, TicketStatus oldStatus) {
        save(
                ticket,
                actor,
                AuditAction.TICKET_CLOSED,
                oldStatus.name(),
                TicketStatus.CLOSED.name(),
                "Обращение закрыто"
        );
    }

    @Transactional
    public void logSlaBreached(TicketEntity ticket, String reason) {
        save(
                ticket,
                null,
                AuditAction.SLA_BREACHED,
                "SLA_OK",
                "SLA_BREACHED",
                reason
        );
    }

    private void save(
            TicketEntity ticket,
            UserEntity actor,
            AuditAction action,
            String oldValue,
            String newValue,
            String message
    ) {
        AuditLogEntity auditLog = AuditLogEntity.builder()
                .ticket(ticket)
                .actor(actor)
                .action(action)
                .oldValue(oldValue)
                .newValue(newValue)
                .message(message)
                .build();

        auditLogRepository.save(auditLog);
    }

    private AuditLogResponse map(AuditLogEntity auditLog) {
        UserEntity actor = auditLog.getActor();

        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getTicket().getId(),
                actor != null ? actor.getId() : null,
                actor != null ? actor.getName() : "SYSTEM",
                actor != null ? actor.getRole() : null,
                auditLog.getAction(),
                auditLog.getOldValue(),
                auditLog.getNewValue(),
                auditLog.getMessage(),
                auditLog.getCreatedAt()
        );
    }

    private String userToString(UserEntity user) {
        if (user == null) {
            return "Без исполнителя";
        }

        return user.getName() + " (" + user.getEmail() + ")";
    }
}