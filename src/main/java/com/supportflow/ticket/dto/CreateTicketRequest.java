package com.supportflow.ticket.dto;

import com.supportflow.ticket.enums.TicketPriority;

public record CreateTicketRequest (
        String title,
        String description,
        TicketPriority priority,
        Long userId
){}
