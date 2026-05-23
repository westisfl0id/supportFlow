package com.supportflow.unittests.service;

import com.supportflow.sla.service.SlaService;
import com.supportflow.ticket.entity.TicketEntity;
import com.supportflow.ticket.enums.TicketPriority;
import com.supportflow.ticket.enums.TicketStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SlaServiceTest {

    private final SlaService slaService = new SlaService();

    @Test
    void calculateFirstResponseDeadline_shouldReturnThirtyMinutesForCriticalPriority() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);

        LocalDateTime result = slaService.calculateFirstResponseDeadline(
                TicketPriority.CRITICAL,
                createdAt
        );

        assertEquals(createdAt.plusMinutes(30), result);
    }

    @Test
    void calculateFirstResponseDeadline_shouldReturnTwoHoursForHighPriority() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);

        LocalDateTime result = slaService.calculateFirstResponseDeadline(
                TicketPriority.HIGH,
                createdAt
        );

        assertEquals(createdAt.plusHours(2), result);
    }

    @Test
    void calculateFirstResponseDeadline_shouldReturnEightHoursForMediumPriority() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);

        LocalDateTime result = slaService.calculateFirstResponseDeadline(
                TicketPriority.MEDIUM,
                createdAt
        );

        assertEquals(createdAt.plusHours(8), result);
    }

    @Test
    void calculateFirstResponseDeadline_shouldReturnTwentyFourHoursForLowPriority() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);

        LocalDateTime result = slaService.calculateFirstResponseDeadline(
                TicketPriority.LOW,
                createdAt
        );

        assertEquals(createdAt.plusHours(24), result);
    }

    @Test
    void calculateResolutionDeadline_shouldReturnFourHoursForCriticalPriority() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);

        LocalDateTime result = slaService.calculateResolutionDeadline(
                TicketPriority.CRITICAL,
                createdAt
        );

        assertEquals(createdAt.plusHours(4), result);
    }

    @Test
    void calculateResolutionDeadline_shouldReturnEightHoursForHighPriority() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);

        LocalDateTime result = slaService.calculateResolutionDeadline(
                TicketPriority.HIGH,
                createdAt
        );

        assertEquals(createdAt.plusHours(8), result);
    }

    @Test
    void calculateResolutionDeadline_shouldReturnTwentyFourHoursForMediumPriority() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1,     1, 10, 0);

        LocalDateTime result = slaService.calculateResolutionDeadline(
                TicketPriority.MEDIUM,
                createdAt
        );

        assertEquals(createdAt.plusHours(24), result);
    }

    @Test
    void calculateResolutionDeadline_shouldReturnSeventyTwoHoursForLowPriority() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);

        LocalDateTime result = slaService.calculateResolutionDeadline(
                TicketPriority.LOW,
                createdAt
        );

        assertEquals(createdAt.plusHours(72), result);
    }

    @Test
    void isSlaBreached_shouldReturnTrueWhenFirstResponseDeadlinePassedAndNoFirstResponse() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);

        TicketEntity ticket = TicketEntity.builder()
                .status(TicketStatus.IN_PROGRESS)
                .firstResponseDeadline(now.minusMinutes(1))
                .resolutionDeadline(now.plusHours(1))
                .firstRespondedAt(null)
                .resolvedAt(null)
                .build();

        assertTrue(slaService.isSlaBreached(ticket, now));
    }

    @Test
    void isSlaBreached_shouldReturnFalseWhenFirstResponseWasRecorded() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);

        TicketEntity ticket = TicketEntity.builder()
                .status(TicketStatus.IN_PROGRESS)
                .firstResponseDeadline(now.minusMinutes(1))
                .resolutionDeadline(now.plusHours(1))
                .firstRespondedAt(now.minusMinutes(30))
                .resolvedAt(null)
                .build();

        assertFalse(slaService.isSlaBreached(ticket, now));
    }

    @Test
    void isSlaBreached_shouldReturnTrueWhenResolutionDeadlinePassedAndTicketNotResolved() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);

        TicketEntity ticket = TicketEntity.builder()
                .status(TicketStatus.IN_PROGRESS)
                .firstResponseDeadline(now.plusHours(1))
                .resolutionDeadline(now.minusMinutes(1))
                .firstRespondedAt(now.minusMinutes(30))
                .resolvedAt(null)
                .build();

        assertTrue(slaService.isSlaBreached(ticket, now));
    }

    @Test
    void isSlaBreached_shouldReturnFalseWhenTicketResolvedBeforeDeadline() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);

        TicketEntity ticket = TicketEntity.builder()
                .status(TicketStatus.RESOLVED)
                .firstResponseDeadline(now.plusHours(1))
                .resolutionDeadline(now.minusMinutes(1))
                .firstRespondedAt(now.minusHours(2))
                .resolvedAt(now.minusMinutes(30))
                .build();

        assertFalse(slaService.isSlaBreached(ticket, now));
    }

    @Test
    void isSlaBreached_shouldReturnFalseForClosedTicketWithPassedResolutionDeadline() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);

        TicketEntity ticket = TicketEntity.builder()
                .status(TicketStatus.CLOSED)
                .firstResponseDeadline(now.plusHours(1))
                .resolutionDeadline(now.minusMinutes(1))
                .firstRespondedAt(now.minusHours(2))
                .resolvedAt(null)
                .build();

        assertFalse(slaService.isSlaBreached(ticket, now));
    }
}
