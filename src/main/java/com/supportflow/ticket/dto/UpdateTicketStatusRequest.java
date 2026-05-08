package com.supportflow.ticket.dto;

import com.supportflow.ticket.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTicketStatusRequest(
        @NotNull
        TicketStatus status
) {
}
