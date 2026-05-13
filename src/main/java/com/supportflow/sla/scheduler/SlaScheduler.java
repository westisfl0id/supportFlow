package com.supportflow.sla.scheduler;

import com.supportflow.sla.service.SlaService;
import com.supportflow.ticket.entity.TicketEntity;
import com.supportflow.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SlaScheduler {
    private final TicketRepository ticketRepository;
    private final SlaService slaService;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkSlaBreaches() {
        LocalDateTime now = LocalDateTime.now();

        List<TicketEntity> tickets = ticketRepository.findAll();

        for (TicketEntity ticket : tickets) {
            if (!Boolean.TRUE.equals(ticket.getSlaBreached()) && slaService.isSlaBreached(ticket, now)) {
                ticket.setSlaBreached(true);
            }
        }
    }
}
