package com.supportflow.ticket.dto;

import com.supportflow.ticket.enums.TicketCategory;
import com.supportflow.ticket.enums.TicketPriority;
import jakarta.validation.constraints.*;

public record CreateTicketRequest (
        @NotBlank(message = "Заголовок обязателен")
        @Size(min = 5, max = 150, message = "Заголовок должен содержать от 5 до 150 символов")
        String title,

        @NotBlank(message = "Описание обязательно")
        @Size(min = 10, max = 3000, message = "Описание должно содержать от 10 до 3000 символов")
        String description,

        @NotNull(message = "Приоритет обязателен")
        TicketPriority priority,

        @NotNull(message = "Категория обязательна")
        TicketCategory category
){}
