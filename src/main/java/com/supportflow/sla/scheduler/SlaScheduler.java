package com.supportflow.sla.scheduler;

import com.supportflow.audit.service.AuditLogService;
import com.supportflow.sla.service.SlaService;
import com.supportflow.ticket.entity.TicketEntity;
import com.supportflow.ticket.enums.TicketStatus;
import com.supportflow.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlaScheduler {
    private final TicketRepository ticketRepository;
    private final SlaService slaService;
    private final AuditLogService auditLogService;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkSlaBreaches() {
        LocalDateTime now = LocalDateTime.now();

        List<TicketEntity> tickets = ticketRepository.findBySlaBreachedFalseAndStatusNot(TicketStatus.CLOSED);

        for (TicketEntity ticket : tickets) {
            if (!Boolean.TRUE.equals(ticket.getSlaBreached()) && slaService.isSlaBreached(ticket, now)) {
                ticket.setSlaBreached(true);

                String reason = buildSlaBreachReason(ticket, now);

                auditLogService.logSlaBreached(ticket, reason);

                log.warn(
                        "SLA breached: ticketId={}, status={}, priority={}, reason={}",
                        ticket.getId(),
                        ticket.getStatus(),
                        ticket.getPriority(),
                        reason
                );
            }
        }
    }

    private String buildSlaBreachReason(TicketEntity ticket, LocalDateTime now) {
        List<String> reasons = new ArrayList<>();

        if (ticket.getFirstRespondedAt() == null
                && ticket.getFirstResponseDeadline() != null
                && now.isAfter(ticket.getFirstResponseDeadline())) {
            reasons.add("первому ответу");
        }

        if (ticket.getResolvedAt() == null
                && ticket.getResolutionDeadline() != null
                && now.isAfter(ticket.getResolutionDeadline())
                && ticket.getStatus() != TicketStatus.CLOSED) {
            reasons.add("сроку решения обращения");
        }

        if (reasons.isEmpty()) {
            return "Нарушен SLA";
        }

        return "Нарушен SLA по " + String.join(" и ", reasons);
    }
}
