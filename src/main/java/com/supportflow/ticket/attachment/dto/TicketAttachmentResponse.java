package com.supportflow.ticket.attachment.dto;

import java.time.LocalDateTime;

public record TicketAttachmentResponse(
        Long id,
        Long ticketId,
        String originalFilename,
        String contentType,
        Long sizeBytes,
        Long uploadedById,
        String uploadedByName,
        LocalDateTime createdAt
) {
}
