    package com.supportflow.ticket.service;

    import com.supportflow.exception.ForbiddenActionException;
    import com.supportflow.security.CurrentUserService;
    import com.supportflow.sla.service.SlaService;
    import com.supportflow.ticket.dto.AssignTicketRequest;
    import com.supportflow.ticket.dto.CreateTicketRequest;
    import com.supportflow.ticket.dto.TicketResponse;
    import com.supportflow.ticket.dto.UpdateTicketStatusRequest;
    import com.supportflow.ticket.entity.TicketEntity;
    import com.supportflow.ticket.enums.TicketCategory;
    import com.supportflow.ticket.enums.TicketPriority;
    import com.supportflow.ticket.enums.TicketStatus;
    import com.supportflow.ticket.exception.TicketAlreadyClosedException;
    import com.supportflow.ticket.exception.TicketNotFoundException;
    import com.supportflow.ticket.repository.TicketRepository;
    import com.supportflow.ticket.specification.TicketSpecification;
    import com.supportflow.user.entity.UserEntity;
    import com.supportflow.user.enums.UserRole;
    import com.supportflow.user.enums.UserStatus;
    import com.supportflow.user.exception.UserIsNotAgentException;
    import com.supportflow.user.exception.UserNotFoundException;
    import com.supportflow.user.repository.UserRepository;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.jpa.domain.Specification;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import java.time.LocalDateTime;
    import java.util.List;

    @Slf4j
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

            log.info("Ticket created: ticketId={}, createdById={}, priority={}", ticket.getId(), currentUser.getId(), ticket.getPriority());

            return map(ticket);
        }

        @Transactional(readOnly = true)
        public Page<TicketResponse> getAllTickets(
                TicketStatus status,
                TicketPriority priority,
                TicketCategory category,
                Long createdById,
                Long assignedToId,
                Pageable pageable) {
            Specification<TicketEntity> specification = Specification
                    .where(TicketSpecification.hasStatus(status))
                    .and(TicketSpecification.hasPriority(priority))
                    .and(TicketSpecification.hasCategory(category))
                    .and(TicketSpecification.hasCreatedById(createdById))
                    .and(TicketSpecification.hasAssignedToId(assignedToId));
            return ticketRepository.findAll(specification, pageable)
                    .map(this::map);
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

            TicketStatus oldStatus = ticket.getStatus();
            LocalDateTime now = LocalDateTime.now();

            if (request.status() == TicketStatus.RESOLVED ) {
                if (ticket.getResolvedAt() == null) {
                    ticket.setResolvedAt(now);
                }

                if (ticket.getFirstRespondedAt() == null
                        && currentUser.getRole() == UserRole.AGENT || currentUser.getRole() == UserRole.ADMIN) {
                    ticket.setResolvedAt(LocalDateTime.now());
                }
            }

            ticket.setStatus(request.status());

            log.info("Ticket status changed: ticketId={}, oldStatus={}, newStatus={}, changedById={}", ticket.getId(), oldStatus, ticket.getStatus(), currentUser.getId());

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

            if (agent.getStatus() != UserStatus.ACTIVE) {
                throw new ForbiddenActionException("Нельзя назначить заблокированного агента");
            }

            if (ticket.getStatus() == TicketStatus.CLOSED) {
                throw new TicketAlreadyClosedException(ticketId);
            }

            UserEntity currentUser = currentUserService.getCurrentUser();

            ticketAccessService.checkCanAssignTicket(currentUser, ticket, agent);

            ticket.setAssignedTo(agent);

            if (ticket.getStatus() == TicketStatus.NEW) {
                ticketStatusTransitionService.validateTransition(TicketStatus.NEW, TicketStatus.OPEN);
                ticket.setStatus(TicketStatus.OPEN);
            }

            log.info("Ticket assigned: ticketId={}, agentId={}, changedById={}", ticket.getId(), agent.getId(), currentUser.getId());

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

            if (ticket.getFirstRespondedAt() == null
                    && (currentUser.getRole() == UserRole.AGENT || currentUser.getRole() == UserRole.ADMIN)) {
                ticket.setStatus(TicketStatus.RESOLVED);
            }

            log.info("Ticket resolved: ticketId={}, resolvedById={}", ticket.getId(), currentUser.getId());

            return map(ticket);
        }

        @Transactional
        public TicketResponse reopenTicket(Long id) {
            TicketEntity ticket = ticketRepository.findById(id)
                    .orElseThrow(() -> new TicketNotFoundException(id));

            UserEntity currentUser = currentUserService.getCurrentUser();

            ticketAccessService.checkCanReopenTicket(currentUser, ticket);
            ticketStatusTransitionService.validateTransition(ticket.getStatus(), TicketStatus.IN_PROGRESS);

            ticket.setStatus(TicketStatus.IN_PROGRESS);
            ticket.setResolvedAt(null);

            log.info("Ticket reopened: ticketId={}, reopenedById={}", ticket.getId(), currentUser.getId());

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

            log.info("Ticket closed: ticketId={}, closedById={}", ticket.getId(), currentUser.getId());

            return map(ticket);
        }

        private TicketEntity buildNewTicket(UserEntity user, CreateTicketRequest request) {
            LocalDateTime now = LocalDateTime.now();

            return TicketEntity.builder()
                    .title(request.title())
                    .description(request.description())
                    .priority(request.priority())
                    .category(request.category())
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
                    ticket.getCategory(),
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
