package com.supportflow.comment.service;

import com.supportflow.comment.dto.CommentResponse;
import com.supportflow.comment.dto.CreateCommentRequest;
import com.supportflow.comment.entity.CommentEntity;
import com.supportflow.comment.repository.CommentRepository;
import com.supportflow.ticket.entity.TicketEntity;
import com.supportflow.ticket.exception.TicketNotFoundException;
import com.supportflow.ticket.repository.TicketRepository;
import com.supportflow.user.entity.UserEntity;
import com.supportflow.user.exception.UserNotFoundException;
import com.supportflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponse create(Long ticketId, Long userId, CreateCommentRequest request) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        CommentEntity comment = CommentEntity.builder()
                .message(request.message())
                .ticket(ticket)
                .createdBy(user)
                .build();

        commentRepository.save(comment);

        return map(comment);
    }

    private CommentResponse map(CommentEntity comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getTicket().getId(),
                comment.getCreatedBy().getId(),
                comment.getCreatedBy().getName(),
                comment.getMessage(),
                comment.getCreatedAt()
        );
    }
}
