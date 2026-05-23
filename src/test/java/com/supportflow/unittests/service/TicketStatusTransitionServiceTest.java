package com.supportflow.unittests.service;

import com.supportflow.ticket.enums.TicketStatus;
import com.supportflow.ticket.exception.InvalidTicketStatusTransitionException;
import com.supportflow.ticket.service.TicketStatusTransitionService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketStatusTransitionServiceTest {

    private final TicketStatusTransitionService ticketStatusTransitionService =
            new TicketStatusTransitionService();

    @Test
    void validateTransition_shouldAllowNewToOpen() {
        assertDoesNotThrow(() ->
                ticketStatusTransitionService.validateTransition(
                        TicketStatus.NEW,
                        TicketStatus.OPEN
                )
        );
    }

    @Test
    void validateTransition_shouldAllowNewToInProgress() {
        assertDoesNotThrow(() ->
                ticketStatusTransitionService.validateTransition(
                        TicketStatus.NEW,
                        TicketStatus.IN_PROGRESS
                )
        );
    }

    @Test
    void validateTransition_shouldRejectNewToClosed() {
        assertThrows(
                InvalidTicketStatusTransitionException.class,
                () -> ticketStatusTransitionService.validateTransition(
                        TicketStatus.NEW,
                        TicketStatus.CLOSED
                )
        );
    }

    @Test
    void validateTransition_shouldAllowInProgressToResolved() {
        assertDoesNotThrow(() ->
                ticketStatusTransitionService.validateTransition(
                        TicketStatus.IN_PROGRESS,
                        TicketStatus.RESOLVED
                )
        );
    }

    @Test
    void validateTransition_shouldAllowResolvedToClosed() {
        assertDoesNotThrow(() ->
                ticketStatusTransitionService.validateTransition(
                        TicketStatus.RESOLVED,
                        TicketStatus.CLOSED
                )
        );
    }

    @Test
    void validateTransition_shouldAllowClosedToInProgress() {
        assertDoesNotThrow(() ->
                ticketStatusTransitionService.validateTransition(
                        TicketStatus.CLOSED,
                        TicketStatus.IN_PROGRESS
                )
        );
    }

    @Test
    void validateTransition_shouldAllowSameStatus() {
        assertDoesNotThrow(() ->
                ticketStatusTransitionService.validateTransition(
                        TicketStatus.IN_PROGRESS,
                        TicketStatus.IN_PROGRESS
                )
        );
    }
}