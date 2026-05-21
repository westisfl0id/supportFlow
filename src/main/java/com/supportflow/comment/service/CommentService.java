package com.supportflow.comment.service;

import com.supportflow.comment.dto.CommentResponse;
import com.supportflow.comment.dto.CreateCommentRequest;
import com.supportflow.comment.entity.CommentEntity;
import com.supportflow.comment.repository.CommentRepository;
import com.supportflow.security.CurrentUserService;
import com.supportflow.ticket.entity.TicketEntity;
import com.supportflow.ticket.enums.TicketStatus;
import com.supportflow.ticket.exception.TicketAlreadyClosedException;
import com.supportflow.ticket.exception.TicketNotFoundException;
import com.supportflow.ticket.repository.TicketRepository;
import com.supportflow.ticket.service.TicketAccessService;
import com.supportflow.user.entity.UserEntity;
import com.supportflow.user.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final CurrentUserService currentUserService;
    private final TicketAccessService ticketAccessService;

    @Transactional
    public CommentResponse createComment(Long ticketId, CreateCommentRequest request) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        UserEntity currentUser = currentUserService.getCurrentUser();

        ticketAccessService.checkCanCommentTicket(currentUser, ticket);

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new TicketAlreadyClosedException(ticketId);
        }

        if ((currentUser.getRole() == UserRole.AGENT || currentUser.getRole() == UserRole.ADMIN)
                && ticket.getFirstRespondedAt() == null) {
            ticket.setFirstRespondedAt(LocalDateTime.now());
        }

        CommentEntity comment = CommentEntity.builder()
                .message(request.message())
                .ticket(ticket)
                .createdBy(currentUser)
                .build();

        commentRepository.save(comment);

        log.info("Comment created: commentId={}, ticketId={}, createdById={}", comment.getId(), ticket.getId(), currentUser.getId());

        return map(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTicket(Long ticketId) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        UserEntity currentUser = currentUserService.getCurrentUser();

        ticketAccessService.checkCanViewTicket(currentUser, ticket);


        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(this::map)
                .toList();
    }

    private CommentResponse map(CommentEntity comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getTicket().getId(),
                comment.getCreatedBy().getId(),
                comment.getCreatedBy().getName(),
                comment.getCreatedBy().getRole(),
                comment.getMessage(),
                comment.getCreatedAt()
        );
    }
}
