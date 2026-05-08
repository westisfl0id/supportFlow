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

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public CommentResponse create(CreateCommentRequest request) {
        TicketEntity ticket = ticketRepository.findById(request.ticketId())
                .orElseThrow(() -> new TicketNotFoundException(request.ticketId()));

        UserEntity user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));

        CommentEntity comment = CommentEntity.builder()
                .message(request.message())
                .ticket(ticket)
                .user(user)
                .build();

        commentRepository.save(comment);

        return new CommentResponse(
                comment.getId(),
                comment.getMessage(),
                user.getName(),
                comment.getCreatedAt()
        );
    }
}
