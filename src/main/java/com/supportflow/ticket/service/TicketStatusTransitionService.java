package com.supportflow.ticket.service;

import com.supportflow.ticket.enums.TicketStatus;
import com.supportflow.ticket.exception.InvalidTicketStatusTransitionException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class TicketStatusTransitionService {
    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS = Map.of(
            TicketStatus.NEW, Set.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS),
            TicketStatus.OPEN, Set.of(TicketStatus.IN_PROGRESS, TicketStatus.WAITING, TicketStatus.RESOLVED),
            TicketStatus.IN_PROGRESS, Set.of(TicketStatus.WAITING, TicketStatus.RESOLVED),
            TicketStatus.WAITING, Set.of(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED),
            TicketStatus.RESOLVED, Set.of(TicketStatus.CLOSED),
            TicketStatus.CLOSED, Set.of()
    );

    public void validateTransition(TicketStatus currentStatus, TicketStatus targetStatus) {
        if (currentStatus == targetStatus) {
            return;
        }

        Set<TicketStatus> allowedTargetStatuses = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());

        if (!allowedTargetStatuses.contains(targetStatus)) {
            throw new InvalidTicketStatusTransitionException(currentStatus, targetStatus);
        }
    }
}
