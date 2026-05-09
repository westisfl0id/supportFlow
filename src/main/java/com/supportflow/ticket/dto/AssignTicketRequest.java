package com.supportflow.ticket.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssignTicketRequest(
        @NotNull
        @Positive
        Long agentId
) {
}
