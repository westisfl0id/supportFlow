package com.supportflow.ticket.dto;

import com.supportflow.ticket.enums.TicketStatus;

public record UpdateTicketStatusRequest(
        TicketStatus status
) {
}
