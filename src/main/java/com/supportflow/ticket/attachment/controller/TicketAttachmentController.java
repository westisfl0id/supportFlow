package com.supportflow.ticket.attachment.controller;

import com.supportflow.ticket.attachment.dto.TicketAttachmentFile;
import com.supportflow.ticket.attachment.dto.TicketAttachmentResponse;
import com.supportflow.ticket.attachment.exception.InvalidAttachmentException;
import com.supportflow.ticket.attachment.service.TicketAttachmentService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class TicketAttachmentController {
    private final TicketAttachmentService attachmentService;

    @PostMapping(
            value = "/tickets/{ticketId}/attachments",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public List<TicketAttachmentResponse> uploadAttachment(
            @PathVariable @Positive Long ticketId,
            @RequestParam("files") List<MultipartFile> files
    ) {
        if (files == null || files.isEmpty()) {
            throw new InvalidAttachmentException("Файлы не выбраны");
        }

        if (files.size() > 5) {
            throw new InvalidAttachmentException("Можно загрузить не больше 5 файлов за раз");
        }

        return files.stream()
                .map(file -> attachmentService.uploadAttachment(ticketId, file))
                .toList();
    }

    @GetMapping("/tickets/{ticketId}/attachments")
    public List<TicketAttachmentResponse> getTicketAttachments(
            @PathVariable @Positive Long ticketId
    ) {
        return attachmentService.getTicketAttachments(ticketId);
    }

    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable @Positive Long attachmentId
    ) {
        TicketAttachmentFile file = attachmentService.downloadAttachment(attachmentId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType() != null
                        ? file.contentType()
                        : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(file.originalFilename(), StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .body(file.resource());
    }
}
