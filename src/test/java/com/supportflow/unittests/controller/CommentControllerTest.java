package com.supportflow.unittests.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportflow.comment.controller.CommentController;
import com.supportflow.comment.dto.CommentResponse;
import com.supportflow.comment.dto.CreateCommentRequest;
import com.supportflow.comment.service.CommentService;
import com.supportflow.exception.GlobalExceptionHandler;
import com.supportflow.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private CommentService commentService;

    @BeforeEach
    void setUp() {
        CommentController commentController = new CommentController(commentService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(commentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /tickets/{ticketId}/comments - создает комментарий")
    void createComment_shouldReturnCreatedComment() throws Exception {
        // Arrange
        CreateCommentRequest request = new CreateCommentRequest(
                "Проблема все еще актуальна"
        );

        CommentResponse response = commentResponse(
                1L,
                10L,
                1L,
                "Test User",
                UserRole.USER,
                "Проблема все еще актуальна"
        );

        when(commentService.createComment(anyLong(), any(CreateCommentRequest.class)))
                .thenReturn(response);

        // Act + Assert
        mockMvc.perform(post("/tickets/10/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ticketId").value(10))
                .andExpect(jsonPath("$.message").value("Проблема все еще актуальна"));

        verify(commentService).createComment(anyLong(), any(CreateCommentRequest.class));
    }

    @Test
    @DisplayName("POST /tickets/{ticketId}/comments - возвращает 400 при пустом сообщении")
    void createComment_shouldReturnBadRequest_whenMessageIsBlank() throws Exception {
        // Arrange
        CreateCommentRequest request = new CreateCommentRequest("");

        // Act + Assert
        mockMvc.perform(post("/tickets/10/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /tickets/{ticketId}/comments - возвращает комментарии заявки")
    void getCommentsByTicket_shouldReturnComments() throws Exception {
        // Arrange
        CommentResponse response = commentResponse(
                1L,
                10L,
                1L,
                "Test User",
                UserRole.USER,
                "Комментарий"
        );

        when(commentService.getCommentsByTicket(10L)).thenReturn(List.of(response));

        // Act + Assert
        mockMvc.perform(get("/tickets/10/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].ticketId").value(10))
                .andExpect(jsonPath("$[0].message").value("Комментарий"));

        verify(commentService).getCommentsByTicket(10L);
    }

    private CommentResponse commentResponse(
            Long id,
            Long ticketId,
            Long createdById,
            String createdByName,
            UserRole createdByRole,
            String message
    ) {
        return new CommentResponse(
                id,
                ticketId,
                createdById,
                createdByName,
                createdByRole,
                message,
                LocalDateTime.of(2026, 1, 1, 12, 0)
        );
    }
}