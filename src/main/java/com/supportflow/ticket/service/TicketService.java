    package com.supportflow.ticket.service;

    import com.supportflow.security.CurrentUserService;
    import com.supportflow.sla.service.SlaService;
    import com.supportflow.ticket.dto.AssignTicketRequest;
    import com.supportflow.ticket.dto.CreateTicketRequest;
    import com.supportflow.ticket.dto.TicketResponse;
    import com.supportflow.ticket.dto.UpdateTicketStatusRequest;
    import com.supportflow.ticket.entity.TicketEntity;
    import com.supportflow.ticket.enums.TicketPriority;
    import com.supportflow.ticket.enums.TicketStatus;
    import com.supportflow.ticket.exception.TicketAlreadyClosedException;
    import com.supportflow.ticket.exception.TicketNotFoundException;
    import com.supportflow.ticket.repository.TicketRepository;
    import com.supportflow.ticket.specification.TicketSpecification;
    import com.supportflow.user.entity.UserEntity;
    import com.supportflow.user.enums.UserRole;
    import com.supportflow.user.exception.UserIsNotAgentException;
    import com.supportflow.user.exception.UserNotFoundException;
    import com.supportflow.user.repository.UserRepository;
    import lombok.RequiredArgsConstructor;
    import org.springframework.data.jpa.domain.Specification;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.time.LocalDateTime;
    import java.util.List;

    @Service
    @RequiredArgsConstructor
    public class TicketService {
        private final TicketRepository ticketRepository;
        private final UserRepository userRepository;
        private final SlaService slaService;
        private final CurrentUserService currentUserService;
        private final TicketAccessService ticketAccessService;
        private final TicketStatusTransitionService ticketStatusTransitionService;

        @Transactional
        public TicketResponse createTicket(Long userId, CreateTicketRequest request) {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));

            TicketEntity ticket = buildNewTicket(user, request);

            ticketRepository.save(ticket);

            return map(ticket);
        }

        @Transactional
        public TicketResponse createTicketForCurrentUser(CreateTicketRequest request) {
            UserEntity currentUser = currentUserService.getCurrentUser();

            TicketEntity ticket = buildNewTicket(currentUser, request);

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
        public List<TicketResponse> getTicketsForCurrentUser() {
            UserEntity currentUser = currentUserService.getCurrentUser();

            return ticketRepository.findByCreatedById(currentUser.getId())
                    .stream()
                    .map(this::map)
                    .toList();
        }

        @Transactional(readOnly = true)
        public TicketResponse getTicketById(Long id) {
            TicketEntity ticket = ticketRepository.findById(id)
                    .orElseThrow(() -> new TicketNotFoundException(id));

            UserEntity currentUser = currentUserService.getCurrentUser();

            ticketAccessService.checkCanViewTicket(currentUser, ticket);

            return map(ticket);
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
        public List<TicketResponse> getSlaBreachedTickets() {
            return ticketRepository.findBySlaBreachedTrue()
                    .stream()
                    .map(this::map)
                    .toList();
        }

        @Transactional
        public TicketResponse updateStatus(Long id, UpdateTicketStatusRequest request) {
            TicketEntity ticket = ticketRepository.findById(id)
                    .orElseThrow(() -> new TicketNotFoundException(id));

            UserEntity currentUser = currentUserService.getCurrentUser();

            ticketAccessService.checkCanManageTicket(currentUser, ticket);
            ticketStatusTransitionService.validateTransition(ticket.getStatus(), request.status());

            if (request.status() == TicketStatus.RESOLVED && ticket.getResolvedAt() == null) {
                ticket.setResolvedAt(LocalDateTime.now());
            }

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

            UserEntity currentUser = currentUserService.getCurrentUser();

            ticketAccessService.checkCanAssignTicket(currentUser, ticket, agent);

            ticket.setAssignedTo(agent);

            return map(ticket);
        }

        @Transactional(readOnly = true)
        public List<TicketResponse> searchTickets(
                TicketStatus status,
                TicketPriority priority,
                Long createdById,
                Long assignedToId
        ) {
            Specification<TicketEntity> spec = Specification
                    .where(TicketSpecification.hasStatus(status))
                    .and(TicketSpecification.hasPriority(priority))
                    .and(TicketSpecification.createdBy(createdById))
                    .and(TicketSpecification.assignedTo(assignedToId));

            return ticketRepository.findAll(spec)
                    .stream()
                    .map(this::map)
                    .toList();
        }

        @Transactional
        public TicketResponse resolveTicket(Long id) {
            TicketEntity ticket = ticketRepository.findById(id)
                    .orElseThrow(() -> new TicketNotFoundException(id));

            UserEntity currentUser = currentUserService.getCurrentUser();

            ticketAccessService.checkCanManageTicket(currentUser, ticket);
            ticketStatusTransitionService.validateTransition(ticket.getStatus(), TicketStatus.RESOLVED);

            if (ticket.getResolvedAt() == null) {
                ticket.setResolvedAt(LocalDateTime.now());
            }

            ticket.setStatus(TicketStatus.RESOLVED);

            return map(ticket);
        }

        @Transactional
        public TicketResponse closeTicket(Long id) {
            TicketEntity ticket = ticketRepository.findById(id)
                    .orElseThrow(() -> new TicketNotFoundException(id));

            if (ticket.getStatus() == TicketStatus.CLOSED) {
                throw new TicketAlreadyClosedException(id);
            }

            UserEntity currentUser = currentUserService.getCurrentUser();

            ticketAccessService.checkCanManageTicket(currentUser, ticket);
            ticketStatusTransitionService.validateTransition(ticket.getStatus(), TicketStatus.CLOSED);

            ticket.setStatus(TicketStatus.CLOSED);

            return map(ticket);
        }

        private TicketEntity buildNewTicket(UserEntity user, CreateTicketRequest request) {
            LocalDateTime now = LocalDateTime.now();

            return TicketEntity.builder()
                    .title(request.title())
                    .description(request.description())
                    .priority(request.priority())
                    .createdBy(user)
                    .status(TicketStatus.NEW)
                    .createdAt(now)
                    .firstResponseDeadline(
                            slaService.calculateFirstResponseDeadline(request.priority(), now)
                    )
                    .resolutionDeadline(
                            slaService.calculateResolutionDeadline(request.priority(), now)
                    )
                    .slaBreached(false)
                    .build();
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
                    ticket.getUpdatedAt(),
                    ticket.getFirstResponseDeadline(),
                    ticket.getResolutionDeadline(),
                    ticket.getFirstRespondedAt(),
                    ticket.getResolvedAt(),
                    ticket.getSlaBreached()
            );
        }
    }
