package com.supportflow.sla.service;

import com.supportflow.ticket.entity.TicketEntity;
import com.supportflow.ticket.enums.TicketPriority;
import com.supportflow.ticket.enums.TicketStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SlaService {
    public LocalDateTime calculateFirstResponseDeadline(TicketPriority priority, LocalDateTime createdAt) {
        return switch (priority) {
            case CRITICAL -> createdAt.plusMinutes(30);
            case HIGH -> createdAt.plusHours(2);
            case MEDIUM -> createdAt.plusHours(8);
            case LOW -> createdAt.plusHours(24);
        };
    }

    public LocalDateTime calculateResolutionDeadline(TicketPriority priority, LocalDateTime createdAt) {
        return switch (priority) {
            case CRITICAL -> createdAt.plusHours(4);
            case HIGH -> createdAt.plusHours(8);
            case MEDIUM -> createdAt.plusHours(24);
            case LOW -> createdAt.plusHours(72);
        };
    }

    public boolean isSlaBreached(TicketEntity ticket, LocalDateTime now) {
        boolean firstResponseBreached =
                ticket.getFirstRespondedAt() == null
                        && ticket.getFirstResponseDeadline() != null
                        && now.isAfter(ticket.getFirstResponseDeadline());

        boolean resolutionBreached =
                ticket.getResolvedAt() == null
                        && ticket.getResolutionDeadline() != null
                        && now.isAfter(ticket.getResolutionDeadline())
                        && ticket.getStatus() != TicketStatus.CLOSED;

        return firstResponseBreached || resolutionBreached;
    }
}
