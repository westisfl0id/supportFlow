package com.supportflow.statistics.service;

import com.supportflow.security.CurrentUserService;
import com.supportflow.statistics.dto.StatisticsResponse;
import com.supportflow.ticket.entity.TicketEntity;
import com.supportflow.ticket.enums.TicketStatus;
import com.supportflow.ticket.repository.TicketRepository;
import com.supportflow.user.entity.UserEntity;
import com.supportflow.user.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final TicketRepository ticketRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public StatisticsResponse getOverview() {
        List<TicketEntity> tickets = ticketRepository.findAll();

        log.info("Statistics overview requested: totalTickets={}", tickets.size());

        return buildStatistics("ADMIN", tickets);
    }

    @Transactional(readOnly = true)
    public StatisticsResponse getMyStatistics() {
        UserEntity currentUser = currentUserService.getCurrentUser();

        List<TicketEntity> tickets;

        if (currentUser.getRole() == UserRole.AGENT) {
            tickets = ticketRepository.findByAssignedToId(currentUser.getId());
        } else if (currentUser.getRole() == UserRole.USER) {
            tickets = ticketRepository.findByCreatedById(currentUser.getId());
        } else {
            tickets = ticketRepository.findAll();
        }

        log.info(
                "User statistics requested: userId={}, role={}, ticketsCount={}",
                currentUser.getId(),
                currentUser.getRole(),
                tickets.size()
        );

        return buildStatistics(currentUser.getRole().name(), tickets);
    }

    private StatisticsResponse buildStatistics(String role, List<TicketEntity> tickets) {
        long total = tickets.size();

        long open = tickets.stream()
                .filter(ticket -> ticket.getStatus() != TicketStatus.RESOLVED)
                .filter(ticket -> ticket.getStatus() != TicketStatus.CLOSED)
                .count();

        long resolved = tickets.stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.RESOLVED)
                .count();

        long closed = tickets.stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.CLOSED)
                .count();

        long slaBreached = tickets.stream()
                .filter(ticket -> Boolean.TRUE.equals(ticket.getSlaBreached()))
                .count();

        Double avgFirstResponse = averageFirstResponseMinutes(tickets);
        Double avgResolution = averageResolutionMinutes(tickets);

        return new StatisticsResponse(
                role,
                total,
                open,
                resolved,
                closed,
                slaBreached,
                avgFirstResponse,
                avgResolution
        );
    }

    private Double averageFirstResponseMinutes(List<TicketEntity> tickets) {
        return tickets.stream()
                .filter(ticket -> ticket.getCreatedAt() != null)
                .filter(ticket -> ticket.getFirstRespondedAt() != null)
                .mapToLong(ticket -> Duration.between(
                        ticket.getCreatedAt(),
                        ticket.getFirstRespondedAt()
                ).toMinutes())
                .average()
                .stream()
                .boxed()
                .findFirst()
                .orElse(null);
    }

    private Double averageResolutionMinutes(List<TicketEntity> tickets) {
        return tickets.stream()
                .filter(ticket -> ticket.getCreatedAt() != null)
                .filter(ticket -> ticket.getResolvedAt() != null)
                .mapToLong(ticket -> Duration.between(
                        ticket.getCreatedAt(),
                        ticket.getResolvedAt()
                ).toMinutes())
                .average()
                .stream()
                .boxed()
                .findFirst()
                .orElse(null);
    }
}
