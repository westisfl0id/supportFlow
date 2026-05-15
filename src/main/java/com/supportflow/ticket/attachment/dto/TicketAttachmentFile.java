package com.supportflow.ticket.attachment.dto;

import org.springframework.core.io.Resource;

public record TicketAttachmentFile(
        Resource resource,
        String contentType,
        String originalFilename
) {
}
