package com.supportflow.ticket.service;

import com.supportflow.ticket.dto.AssignTicketRequest;
import com.supportflow.ticket.dto.CreateTicketRequest;
import com.supportflow.ticket.dto.TicketResponse;
import com.supportflow.ticket.dto.UpdateTicketStatusRequest;
import com.supportflow.ticket.entity.TicketEntity;
import com.supportflow.ticket.enums.TicketStatus;
import com.supportflow.ticket.exception.TicketNotFoundException;
import com.supportflow.ticket.repository.TicketRepository;
import com.supportflow.user.entity.UserEntity;
import com.supportflow.user.enums.UserRole;
import com.supportflow.user.exception.UserIsNotAgentException;
import com.supportflow.user.exception.UserNotFoundException;
import com.supportflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Transactional
    public TicketResponse createTicket(Long userId, CreateTicketRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

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

    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        return ticketRepository.findByCreatedById(userId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByAgent(Long agentId) {
        UserEntity agent = userRepository.findById(agentId)
                .orElseThrow(() -> new UserNotFoundException(agentId));

        if (agent.getRole() != UserRole.AGENT) {
            throw new UserIsNotAgentException(agentId);
        }

        return ticketRepository.findByAssignedToId(agentId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id) {
        TicketEntity ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));

        return map(ticket);
    }

    @Transactional
    public TicketResponse updateStatus(Long id, UpdateTicketStatusRequest request) {
        TicketEntity ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));

        ticket.setStatus(request.status());

        return map(ticket);
    }

    @Transactional
    public TicketResponse assignTicket(Long ticketId, AssignTicketRequest request) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        UserEntity agent = userRepository.findById(request.agentId())
                .orElseThrow(() -> new UserNotFoundException(request.agentId()));

        if (agent.getRole() != UserRole.AGENT) {
            throw new UserIsNotAgentException(request.agentId());
        }

        ticket.setAssignedTo(agent);

        return map(ticket);
    }

    private TicketResponse map(TicketEntity ticket) {
        UserEntity assignedTo = ticket.getAssignedTo();

        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCreatedBy().getId(),
                ticket.getCreatedBy().getName(),
                assignedTo != null ? assignedTo.getId() : null,
                assignedTo != null ? assignedTo.getName() : null,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }
}
