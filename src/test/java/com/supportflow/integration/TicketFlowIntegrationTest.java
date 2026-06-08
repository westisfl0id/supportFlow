package com.supportflow.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TicketFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Пользователь должен зарегистрироваться и создать заявку")
    void userShouldRegisterAndCreateTicket() throws Exception {
        String email = "ticket-user-" + UUID.randomUUID() + "@test.com";

        String registerJson = """
                {
                  "name": "Ticket User",
                  "email": "%s",
                  "password": "123456"
                }
                """.formatted(email);

        MvcResult registerResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode registerResponse = objectMapper.readTree(
                registerResult.getResponse().getContentAsString()
        );

        String token = registerResponse.get("token").asText();

        String ticketJson = """
                {
                  "title": "Не работает доступ",
                  "description": "Пользователь не может войти в корпоративную систему",
                  "priority": "HIGH",
                  "category": "ACCESS"
                }
                """;

        mockMvc.perform(post("/tickets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ticketJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Не работает доступ"))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.category").value("ACCESS"))
                .andExpect(jsonPath("$.slaBreached").value(false));
    }
}