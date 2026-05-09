package com.supportflow.ticket.dto;

import com.supportflow.ticket.enums.TicketPriority;
import com.supportflow.ticket.enums.TicketStatus;
import java.time.LocalDateTime;

public record TicketResponse (
        Long id,
        String title,
        String description,
        TicketStatus status,
        TicketPriority priority,
        Long createById,
        String createdByName,
        Long assignedToId,
        String assignedToName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){
}
