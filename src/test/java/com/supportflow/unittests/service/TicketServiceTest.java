package com.supportflow.unittests.service;

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
import com.supportflow.ticket.service.TicketAccessService;
import com.supportflow.ticket.service.TicketService;
import com.supportflow.ticket.service.TicketStatusTransitionService;
import com.supportflow.user.entity.UserEntity;
import com.supportflow.user.enums.UserRole;
import com.supportflow.user.enums.UserStatus;
import com.supportflow.user.exception.UserIsNotAgentException;
import com.supportflow.user.exception.UserNotFoundException;
import com.supportflow.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import com.supportflow.audit.service.AuditLogService;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {
    private static final TicketCategory TEST_CATEGORY = TicketCategory.values()[0];

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SlaService slaService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private TicketAccessService ticketAccessService;

    @Mock
    private TicketStatusTransitionService ticketStatusTransitionService;

    @InjectMocks
    private TicketService ticketService;

    @Test
    @DisplayName("createTicketForCurrentUser - создает заявку текущего пользователя и рассчитывает SLA")
    void createTicketForCurrentUser_shouldCreateTicketWithSlaDeadlines() {
        // Arrange
        UserEntity currentUser = user(1L, "Test User", UserRole.USER);

        CreateTicketRequest request = new CreateTicketRequest(
                "Не работает принтер",
                "При печати появляется ошибка",
                TicketPriority.HIGH,
                TEST_CATEGORY.HARDWARE
        );

        LocalDateTime firstResponseDeadline = LocalDateTime.of(2026, 1, 1, 12, 0);
        LocalDateTime resolutionDeadline = LocalDateTime.of(2026, 1, 1, 18, 0);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(slaService.calculateFirstResponseDeadline(eq(TicketPriority.HIGH), any(LocalDateTime.class)))
                .thenReturn(firstResponseDeadline);
        when(slaService.calculateResolutionDeadline(eq(TicketPriority.HIGH), any(LocalDateTime.class)))
                .thenReturn(resolutionDeadline);

        // Имитируем сохранение JPA: после save() у заявки появляется id.
        when(ticketRepository.save(any(TicketEntity.class))).thenAnswer(invocation -> {
            TicketEntity ticket = invocation.getArgument(0);
            ticket.setId(10L);
            return ticket;
        });

        // Act
        TicketResponse response = ticketService.createTicketForCurrentUser(request);

        // Assert
        assertEquals(10L, response.id());
        assertEquals("Не работает принтер", response.title());
        assertEquals(TicketStatus.NEW, response.status());
        assertEquals(TicketPriority.HIGH, response.priority());
        assertEquals(1L, response.createById());
        assertEquals(firstResponseDeadline, response.firstResponseDeadline());
        assertEquals(resolutionDeadline, response.resolutionDeadline());
        assertFalse(response.slaBreached());

        ArgumentCaptor<TicketEntity> ticketCaptor = ArgumentCaptor.forClass(TicketEntity.class);
        verify(ticketRepository).save(ticketCaptor.capture());

        TicketEntity savedTicket = ticketCaptor.getValue();

        assertEquals("Не работает принтер", savedTicket.getTitle());
        assertEquals(TicketStatus.NEW, savedTicket.getStatus());
        assertEquals(currentUser, savedTicket.getCreatedBy());
        assertEquals(firstResponseDeadline, savedTicket.getFirstResponseDeadline());
        assertEquals(resolutionDeadline, savedTicket.getResolutionDeadline());
        assertFalse(savedTicket.getSlaBreached());

        verify(currentUserService).getCurrentUser();
        verify(slaService).calculateFirstResponseDeadline(eq(TicketPriority.HIGH), any(LocalDateTime.class));
        verify(slaService).calculateResolutionDeadline(eq(TicketPriority.HIGH), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("getTicketById - возвращает заявку если пользователь имеет доступ")
    void getTicketById_shouldReturnTicket_whenUserHasAccess() {
        // Arrange
        UserEntity owner = user(1L, "Test User", UserRole.USER);
        TicketEntity ticket = ticket(10L, owner, null, TicketStatus.NEW);

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(currentUserService.getCurrentUser()).thenReturn(owner);

        // Act
        TicketResponse response = ticketService.getTicketById(10L);

        // Assert
        assertEquals(10L, response.id());
        assertEquals("Test ticket", response.title());

        verify(ticketAccessService).checkCanViewTicket(owner, ticket);
    }

    @Test
    @DisplayName("getTicketById - ошибка если заявка не найдена")
    void getTicketById_shouldThrowTicketNotFoundException_whenTicketDoesNotExist() {
        // Arrange
        when(ticketRepository.findById(10L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                TicketNotFoundException.class,
                () -> ticketService.getTicketById(10L)
        );

        verify(currentUserService, never()).getCurrentUser();
    }

    @Test
    @DisplayName("getTicketsForCurrentUser - возвращает заявки текущего пользователя")
    void getTicketsForCurrentUser_shouldReturnCurrentUserTickets() {
        // Arrange
        UserEntity currentUser = user(1L, "Test User", UserRole.USER);
        TicketEntity ticket1 = ticket(10L, currentUser, null, TicketStatus.NEW);
        TicketEntity ticket2 = ticket(11L, currentUser, null, TicketStatus.IN_PROGRESS);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(ticketRepository.findByCreatedById(1L)).thenReturn(List.of(ticket1, ticket2));

        // Act
        List<TicketResponse> response = ticketService.getTicketsForCurrentUser();

        // Assert
        assertEquals(2, response.size());
        assertEquals(10L, response.get(0).id());
        assertEquals(11L, response.get(1).id());

        verify(ticketRepository).findByCreatedById(1L);
    }

    @Test
    @DisplayName("assignTicket - назначает заявку на агента")
    void assignTicket_shouldAssignTicketToAgent() {
        // Arrange
        UserEntity owner = user(1L, "Test User", UserRole.USER);
        UserEntity currentAgent = user(2L, "Support Agent", UserRole.AGENT);
        UserEntity targetAgent = currentAgent;

        TicketEntity ticket = ticket(10L, owner, null, TicketStatus.NEW);

        AssignTicketRequest request = new AssignTicketRequest(2L);

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetAgent));
        when(currentUserService.getCurrentUser()).thenReturn(currentAgent);

        // Act
        TicketResponse response = ticketService.assignTicket(10L, request);

        // Assert
        assertEquals(2L, response.assignedToId());
        assertEquals("Support Agent", response.assignedToName());
        assertEquals(targetAgent, ticket.getAssignedTo());

        verify(ticketAccessService).checkCanAssignTicket(currentAgent, ticket, targetAgent);
    }

    @Test
    @DisplayName("assignTicket - ошибка если заявка не найдена")
    void assignTicket_shouldThrowTicketNotFoundException_whenTicketDoesNotExist() {
        // Arrange
        AssignTicketRequest request = new AssignTicketRequest(2L);

        when(ticketRepository.findById(10L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                TicketNotFoundException.class,
                () -> ticketService.assignTicket(10L, request)
        );

        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("assignTicket - ошибка если пользователь не найден")
    void assignTicket_shouldThrowUserNotFoundException_whenAgentDoesNotExist() {
        // Arrange
        UserEntity owner = user(1L, "Test User", UserRole.USER);
        TicketEntity ticket = ticket(10L, owner, null, TicketStatus.NEW);
        AssignTicketRequest request = new AssignTicketRequest(2L);

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                UserNotFoundException.class,
                () -> ticketService.assignTicket(10L, request)
        );

        verify(currentUserService, never()).getCurrentUser();
    }

    @Test
    @DisplayName("assignTicket - ошибка если выбранный пользователь не агент")
    void assignTicket_shouldThrowUserIsNotAgentException_whenTargetUserIsNotAgent() {
        // Arrange
        UserEntity owner = user(1L, "Test User", UserRole.USER);
        UserEntity notAgent = user(2L, "Regular User", UserRole.USER);
        TicketEntity ticket = ticket(10L, owner, null, TicketStatus.NEW);
        AssignTicketRequest request = new AssignTicketRequest(2L);

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(notAgent));

        // Act + Assert
        assertThrows(
                UserIsNotAgentException.class,
                () -> ticketService.assignTicket(10L, request)
        );

        verify(currentUserService, never()).getCurrentUser();
    }

    @Test
    @DisplayName("updateStatus - меняет статус и фиксирует resolvedAt при RESOLVED")
    void updateStatus_shouldUpdateStatusAndSetResolvedAt_whenTargetStatusIsResolved() {
        // Arrange
        UserEntity owner = user(1L, "Test User", UserRole.USER);
        UserEntity agent = user(2L, "Support Agent", UserRole.AGENT);

        TicketEntity ticket = ticket(10L, owner, agent, TicketStatus.IN_PROGRESS);
        ticket.setResolvedAt(null);

        UpdateTicketStatusRequest request = new UpdateTicketStatusRequest(TicketStatus.RESOLVED);

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(currentUserService.getCurrentUser()).thenReturn(agent);

        // Act
        TicketResponse response = ticketService.updateStatus(10L, request);

        // Assert
        assertEquals(TicketStatus.RESOLVED, response.status());
        assertNotNull(response.resolvedAt());
        assertEquals(TicketStatus.RESOLVED, ticket.getStatus());
        assertNotNull(ticket.getResolvedAt());

        verify(ticketAccessService).checkCanManageTicket(agent, ticket);
        verify(ticketStatusTransitionService).validateTransition(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED);
    }

    @Test
    @DisplayName("resolveTicket - переводит заявку в RESOLVED")
    void resolveTicket_shouldSetResolvedStatusAndResolvedAt() {
        // Arrange
        UserEntity owner = user(1L, "Test User", UserRole.USER);
        UserEntity agent = user(2L, "Support Agent", UserRole.AGENT);

        TicketEntity ticket = ticket(10L, owner, agent, TicketStatus.IN_PROGRESS);
        ticket.setResolvedAt(null);

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(currentUserService.getCurrentUser()).thenReturn(agent);

        // Act
        TicketResponse response = ticketService.resolveTicket(10L);

        // Assert
        assertEquals(TicketStatus.RESOLVED, response.status());
        assertNotNull(response.resolvedAt());

        verify(ticketAccessService).checkCanManageTicket(agent, ticket);
        verify(ticketStatusTransitionService).validateTransition(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED);
    }

    @Test
    @DisplayName("closeTicket - закрывает решенную заявку")
    void closeTicket_shouldSetClosedStatus_whenTicketIsResolved() {
        // Arrange
        UserEntity owner = user(1L, "Test User", UserRole.USER);
        UserEntity agent = user(2L, "Support Agent", UserRole.AGENT);

        TicketEntity ticket = ticket(10L, owner, agent, TicketStatus.RESOLVED);

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(currentUserService.getCurrentUser()).thenReturn(agent);

        // Act
        TicketResponse response = ticketService.closeTicket(10L);

        // Assert
        assertEquals(TicketStatus.CLOSED, response.status());
        assertEquals(TicketStatus.CLOSED, ticket.getStatus());

        verify(ticketAccessService).checkCanManageTicket(agent, ticket);
        verify(ticketStatusTransitionService).validateTransition(TicketStatus.RESOLVED, TicketStatus.CLOSED);
    }

    @Test
    @DisplayName("closeTicket - ошибка если заявка уже закрыта")
    void closeTicket_shouldThrowTicketAlreadyClosedException_whenTicketAlreadyClosed() {
        // Arrange
        UserEntity owner = user(1L, "Test User", UserRole.USER);
        TicketEntity ticket = ticket(10L, owner, null, TicketStatus.CLOSED);

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        // Act + Assert
        assertThrows(
                TicketAlreadyClosedException.class,
                () -> ticketService.closeTicket(10L)
        );

        verify(currentUserService, never()).getCurrentUser();
        verify(ticketStatusTransitionService, never()).validateTransition(any(), any());
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
                .firstResponseDeadline(LocalDateTime.of(2026, 1, 1, 18, 0))
                .resolutionDeadline(LocalDateTime.of(2026, 1, 2, 10, 0))
                .slaBreached(false)
                .build();
    }
}
