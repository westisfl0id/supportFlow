package com.supportflow.ticket.dto;

import com.supportflow.ticket.enums.TicketPriority;
import com.supportflow.ticket.enums.TicketStatus;

public record TicketResponse (
        Long id,
        String title,
        String description,
        TicketStatus status,
        TicketPriority priority,
        String createdBy
){
}
