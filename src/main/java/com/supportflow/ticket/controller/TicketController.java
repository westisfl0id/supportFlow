package com.supportflow.ticket.controller;

import com.supportflow.ticket.dto.CreateTicketRequest;
import com.supportflow.ticket.dto.TicketResponse;
import com.supportflow.ticket.dto.UpdateTicketStatusRequest;
import com.supportflow.ticket.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;

    @PostMapping
    public TicketResponse create(@Valid @RequestBody CreateTicketRequest request) {
        return ticketService.createTicket(request);
    }

    @PatchMapping("/{id}/status")
    public TicketResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketStatusRequest request) {
        return ticketService.updateStatus(id, request);
    }
}
