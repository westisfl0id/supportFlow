package com.supportflow.audit.controller;

import com.supportflow.audit.dto.AuditLogResponse;
import com.supportflow.audit.service.AuditLogService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class AuditLogController {
    private final AuditLogService auditLogService;

    @GetMapping("/tickets/{ticketId}/timeline")
    public List<AuditLogResponse> getTicketTimeline(
            @PathVariable @Positive Long ticketId
    ) {
        return auditLogService.getTicketTimeline(ticketId);
    }
}