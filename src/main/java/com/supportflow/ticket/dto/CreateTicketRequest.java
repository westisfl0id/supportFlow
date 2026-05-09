package com.supportflow.ticket.dto;

import com.supportflow.ticket.enums.TicketPriority;
import jakarta.validation.constraints.*;

public record CreateTicketRequest (
        @NotBlank
        String title,

        @NotBlank
        String description,

        @NotNull
        TicketPriority priority
){}
