package com.supportflow.unittests.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportflow.exception.GlobalExceptionHandler;
import com.supportflow.ticket.controller.TicketController;
import com.supportflow.ticket.dto.CreateTicketRequest;
import com.supportflow.ticket.dto.TicketResponse;
import com.supportflow.ticket.dto.UpdateTicketStatusRequest;
import com.supportflow.ticket.enums.TicketCategory;
import com.supportflow.ticket.enums.TicketPriority;
import com.supportflow.ticket.enums.TicketStatus;
import com.supportflow.ticket.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    private static final TicketCategory TEST_CATEGORY = TicketCategory.values()[0];

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        TicketController ticketController = new TicketController(ticketService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(ticketController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /tickets - создает заявку текущего пользователя")
    void createForCurrentUser_shouldReturnCreatedTicket() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest(
                "Не работает принтер",
                "Ошибка при печати",
                TicketPriority.MEDIUM,
                TEST_CATEGORY
        );

        TicketResponse response = ticketResponse(
                1L,
                "Не работает принтер",
                TicketStatus.NEW,
                TicketPriority.MEDIUM
        );

        when(ticketService.createTicketForCurrentUser(any(CreateTicketRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Не работает принтер"))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));

        verify(ticketService).createTicketForCurrentUser(any(CreateTicketRequest.class));
    }

    @Test
    @DisplayName("POST /tickets - возвращает 400 при пустой теме")
    void createForCurrentUser_shouldReturnBadRequest_whenTitleIsBlank() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest(
                "",
                "Описание проблемы",
                TicketPriority.MEDIUM,
                TEST_CATEGORY
        );

        mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(ticketService, never()).createTicketForCurrentUser(any(CreateTicketRequest.class));
    }

    @Test
    @DisplayName("GET /tickets/my - возвращает заявки текущего пользователя")
    void getMyTickets_shouldReturnTickets() throws Exception {
        TicketResponse ticket = ticketResponse(
                1L,
                "Не работает принтер",
                TicketStatus.NEW,
                TicketPriority.MEDIUM
        );

        when(ticketService.getTicketsForCurrentUser()).thenReturn(List.of(ticket));

        mockMvc.perform(get("/tickets/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Не работает принтер"));

        verify(ticketService).getTicketsForCurrentUser();
    }

    @Test
    @DisplayName("GET /tickets - возвращает список всех заявок")
    void getAllTickets_shouldReturnTickets() throws Exception {
        TicketResponse ticket = ticketResponse(
                1L,
                "Не работает принтер",
                TicketStatus.NEW,
                TicketPriority.MEDIUM
        );

        when(ticketService.getAllTickets(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(
                List.of(ticket),
                PageRequest.of(0, 20),
                1
        ));

        mockMvc.perform(get("/tickets")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));

        verify(ticketService).getAllTickets(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)
        );
    }

    @Test
    @DisplayName("GET /tickets/{id} - возвращает заявку по id")
    void getTicketById_shouldReturnTicket() throws Exception {
        TicketResponse ticket = ticketResponse(
                1L,
                "Не работает принтер",
                TicketStatus.NEW,
                TicketPriority.MEDIUM
        );

        when(ticketService.getTicketById(1L)).thenReturn(ticket);

        mockMvc.perform(get("/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Не работает принтер"));

        verify(ticketService).getTicketById(1L);
    }

    @Test
    @DisplayName("PATCH /tickets/{id}/status - меняет статус заявки")
    void updateStatus_shouldReturnUpdatedTicket() throws Exception {
        UpdateTicketStatusRequest request = new UpdateTicketStatusRequest(TicketStatus.IN_PROGRESS);

        TicketResponse response = ticketResponse(
                1L,
                "Не работает принтер",
                TicketStatus.IN_PROGRESS,
                TicketPriority.MEDIUM
        );

        when(ticketService.updateStatus(anyLong(), any(UpdateTicketStatusRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/tickets/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        verify(ticketService).updateStatus(anyLong(), any(UpdateTicketStatusRequest.class));
    }

    @Test
    @DisplayName("PATCH /tickets/{id}/resolve - решает заявку")
    void resolveTicket_shouldReturnResolvedTicket() throws Exception {
        TicketResponse response = ticketResponse(
                1L,
                "Не работает принтер",
                TicketStatus.RESOLVED,
                TicketPriority.MEDIUM
        );

        when(ticketService.resolveTicket(1L)).thenReturn(response);

        mockMvc.perform(patch("/tickets/1/resolve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));

        verify(ticketService).resolveTicket(1L);
    }

    @Test
    @DisplayName("PATCH /tickets/{id}/close - закрывает заявку")
    void closeTicket_shouldReturnClosedTicket() throws Exception {
        TicketResponse response = ticketResponse(
                1L,
                "Не работает принтер",
                TicketStatus.CLOSED,
                TicketPriority.MEDIUM
        );

        when(ticketService.closeTicket(1L)).thenReturn(response);

        mockMvc.perform(patch("/tickets/1/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        verify(ticketService).closeTicket(1L);
    }

    private TicketResponse ticketResponse(
            Long id,
            String title,
            TicketStatus status,
            TicketPriority priority
    ) {
        return new TicketResponse(
                id,
                title,
                "Описание заявки",
                status,
                priority,
                TEST_CATEGORY,
                1L,
                "Test User",
                null,
                null,
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 1, 18, 0),
                LocalDateTime.of(2026, 1, 2, 10, 0),
                null,
                null,
                false
        );
    }
}