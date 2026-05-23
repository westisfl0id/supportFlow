package com.supportflow.unittests.service;

import com.supportflow.audit.service.AuditLogService;
import com.supportflow.comment.dto.CommentResponse;
import com.supportflow.comment.dto.CreateCommentRequest;
import com.supportflow.comment.entity.CommentEntity;
import com.supportflow.comment.repository.CommentRepository;
import com.supportflow.comment.service.CommentService;
import com.supportflow.security.CurrentUserService;
import com.supportflow.ticket.entity.TicketEntity;
import com.supportflow.ticket.enums.TicketPriority;
import com.supportflow.ticket.enums.TicketStatus;
import com.supportflow.ticket.exception.TicketAlreadyClosedException;
import com.supportflow.ticket.exception.TicketNotFoundException;
import com.supportflow.ticket.repository.TicketRepository;
import com.supportflow.ticket.service.TicketAccessService;
import com.supportflow.user.entity.UserEntity;
import com.supportflow.user.enums.UserRole;
import com.supportflow.user.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private TicketAccessService ticketAccessService;

    @InjectMocks
    private CommentService commentService;

    @Test
    @DisplayName("createComment - пользователь оставляет комментарий в своей заявке")
    void createComment_shouldCreateComment_whenUserHasAccess() {
        // Arrange
        UserEntity user = user(1L, "Test User", UserRole.USER);
        TicketEntity ticket = ticket(10L, user, null, TicketStatus.IN_PROGRESS);

        CreateCommentRequest request = new CreateCommentRequest("Проблема все еще актуальна");

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(currentUserService.getCurrentUser()).thenReturn(user);

        // Имитируем JPA: после save() у комментария появляется id и createdAt.
        when(commentRepository.save(any(CommentEntity.class))).thenAnswer(invocation -> {
            CommentEntity comment = invocation.getArgument(0);
            comment.setId(100L);
            comment.setCreatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
            return comment;
        });

        // Act
        CommentResponse response = commentService.createComment(10L, request);

        // Assert
        assertEquals(100L, response.id());
        assertEquals(10L, response.ticketId());
        assertEquals(1L, response.createdById());
        assertEquals("Test User", response.createdByName());
        assertEquals(UserRole.USER, response.createdByRole());
        assertEquals("Проблема все еще актуальна", response.message());

        verify(ticketAccessService).checkCanCommentTicket(user, ticket);
        verify(commentRepository).save(any(CommentEntity.class));
    }

    @Test
    @DisplayName("createComment - первый ответ агента фиксирует firstRespondedAt")
    void createComment_shouldSetFirstRespondedAt_whenAgentCommentsFirstTime() {
        // Arrange
        UserEntity owner = user(1L, "Test User", UserRole.USER);
        UserEntity agent = user(2L, "Support Agent", UserRole.AGENT);

        TicketEntity ticket = ticket(10L, owner, agent, TicketStatus.IN_PROGRESS);
        ticket.setFirstRespondedAt(null);

        CreateCommentRequest request = new CreateCommentRequest("Здравствуйте, заявка принята в работу");

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(currentUserService.getCurrentUser()).thenReturn(agent);

        when(commentRepository.save(any(CommentEntity.class))).thenAnswer(invocation -> {
            CommentEntity comment = invocation.getArgument(0);
            comment.setId(100L);
            comment.setCreatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
            return comment;
        });

        // Act
        CommentResponse response = commentService.createComment(10L, request);

        // Assert
        assertEquals(100L, response.id());
        assertNotNull(ticket.getFirstRespondedAt());

        verify(ticketAccessService).checkCanCommentTicket(agent, ticket);
        verify(commentRepository).save(any(CommentEntity.class));
    }

    @Test
    @DisplayName("createComment - нельзя комментировать закрытую заявку")
    void createComment_shouldThrowTicketAlreadyClosedException_whenTicketIsClosed() {
        // Arrange
        UserEntity user = user(1L, "Test User", UserRole.USER);
        TicketEntity ticket = ticket(10L, user, null, TicketStatus.CLOSED);

        CreateCommentRequest request = new CreateCommentRequest("Комментарий");

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(currentUserService.getCurrentUser()).thenReturn(user);

        // Act + Assert
        assertThrows(
                TicketAlreadyClosedException.class,
                () -> commentService.createComment(10L, request)
        );

        verify(ticketAccessService).checkCanCommentTicket(user, ticket);
        verify(commentRepository, never()).save(any(CommentEntity.class));
    }

    @Test
    @DisplayName("createComment - ошибка если заявка не найдена")
    void createComment_shouldThrowTicketNotFoundException_whenTicketDoesNotExist() {
        // Arrange
        CreateCommentRequest request = new CreateCommentRequest("Комментарий");

        when(ticketRepository.findById(10L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                TicketNotFoundException.class,
                () -> commentService.createComment(10L, request)
        );

        verify(currentUserService, never()).getCurrentUser();
        verify(commentRepository, never()).save(any(CommentEntity.class));
    }

    @Test
    @DisplayName("getCommentsByTicket - возвращает комментарии заявки")
    void getCommentsByTicket_shouldReturnComments_whenUserHasAccess() {
        // Arrange
        UserEntity user = user(1L, "Test User", UserRole.USER);
        TicketEntity ticket = ticket(10L, user, null, TicketStatus.IN_PROGRESS);

        CommentEntity comment1 = comment(100L, ticket, user, "Первый комментарий");
        CommentEntity comment2 = comment(101L, ticket, user, "Второй комментарий");

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(commentRepository.findByTicketIdOrderByCreatedAtAsc(10L))
                .thenReturn(List.of(comment1, comment2));

        // Act
        List<CommentResponse> response = commentService.getCommentsByTicket(10L);

        // Assert
        assertEquals(2, response.size());
        assertEquals("Первый комментарий", response.get(0).message());
        assertEquals("Второй комментарий", response.get(1).message());

        verify(ticketAccessService).checkCanViewTicket(user, ticket);
        verify(commentRepository).findByTicketIdOrderByCreatedAtAsc(10L);
    }

    private UserEntity user(Long id, String name, UserRole role) {
        return UserEntity.builder()
                .id(id)
                .name(name)
                .email("user" + id + "@test.com")
                .password("encoded-password")
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();
    }

    private TicketEntity ticket(Long id, UserEntity createdBy, UserEntity assignedTo, TicketStatus status) {
        return TicketEntity.builder()
                .id(id)
                .title("Test ticket")
                .description("Test description")
                .priority(TicketPriority.MEDIUM)
                .status(status)
                .createdBy(createdBy)
                .assignedTo(assignedTo)
                .createdAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .slaBreached(false)
                .build();
    }

    private CommentEntity comment(Long id, TicketEntity ticket, UserEntity createdBy, String message) {
        return CommentEntity.builder()
                .id(id)
                .ticket(ticket)
                .createdBy(createdBy)
                .message(message)
                .createdAt(LocalDateTime.of(2026, 1, 1, 12, 0))
                .build();
    }
}
