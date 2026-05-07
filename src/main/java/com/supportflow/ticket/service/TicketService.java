package com.supportflow.ticket.service;

import com.supportflow.ticket.dto.CreateTicketRequest;
import com.supportflow.ticket.dto.TicketResponse;
import com.supportflow.ticket.dto.UpdateTicketStatusRequest;
import com.supportflow.ticket.entity.TicketEntity;
import com.supportflow.ticket.enums.TicketStatus;
import com.supportflow.ticket.exception.TicketNotFoundException;
import com.supportflow.ticket.repository.TicketRepository;
import com.supportflow.user.entity.UserEntity;
import com.supportflow.user.exception.UserNotFoundException;
import com.supportflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public TicketResponse createTicket(CreateTicketRequest request) {
        UserEntity user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));

        TicketEntity ticket = TicketEntity.builder()
                .title(request.title())
                .description(request.description())
                .priority(request.priority())
                .createdBy(user)
                .status(TicketStatus.NEW)
                .build();

        ticketRepository.save(ticket);

        return map(ticket);
    }

    public TicketResponse updateStatus(Long id, UpdateTicketStatusRequest request) {
        TicketEntity ticket = ticketRepository.findById(id)
                .orElseThrow(TicketNotFoundException::new);

        ticket.setStatus(request.status());
        return map(ticket);
    }

    private TicketResponse map(TicketEntity t) {
        return new TicketResponse(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus(),
                t.getPriority(),
                t.getCreatedBy().getName()
        );
    }

}
