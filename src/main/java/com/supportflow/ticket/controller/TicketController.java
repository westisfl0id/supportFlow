package com.supportflow.ticket.controller;

import com.supportflow.ticket.dto.AssignTicketRequest;
import com.supportflow.ticket.dto.CreateTicketRequest;
import com.supportflow.ticket.dto.TicketResponse;
import com.supportflow.ticket.dto.UpdateTicketStatusRequest;
import com.supportflow.ticket.enums.TicketPriority;
import com.supportflow.ticket.enums.TicketStatus;
import com.supportflow.ticket.service.TicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class TicketController {
    private final TicketService ticketService;

    @PostMapping("/users/{userId}/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse create(
            @PathVariable @Positive Long userId,
            @Valid @RequestBody CreateTicketRequest request
    ) {
        return ticketService.createTicket(userId, request);
    }

    @GetMapping("/tickets")
    public List<TicketResponse> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @GetMapping("/tickets/search")
    public List<TicketResponse> searchTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) @Positive Long createdById,
            @RequestParam(required = false) @Positive Long assignedToId
            ) {
        return ticketService.searchTickets(status, priority, createdById, assignedToId);
    }

    @GetMapping("/users/{userId}/tickets")
    public List<TicketResponse> getTicketsByUser(@PathVariable @Positive Long userId) {
        return ticketService.getTicketsByUser(userId);
    }

    @GetMapping("/agents/{agentId}/tickets")
    public List<TicketResponse> getTicketByAgent(@PathVariable @Positive Long agentId) {
        return ticketService.getTicketsByAgent(agentId);
    }

    @GetMapping("/tickets/{id}")
    public TicketResponse getTicketById(@PathVariable @Positive Long id) {
        return ticketService.getTicketById(id);
    }

    @PatchMapping("/tickets/{id}/status")
    public TicketResponse updateStatus(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateTicketStatusRequest request) {
        return ticketService.updateStatus(id, request);
    }

    @PatchMapping("/tickets/{id}/resolve")
    public TicketResponse resolveTicket(@PathVariable @Positive Long id) {
        return ticketService.resolveTicket(id);
    }

    @PatchMapping("/tickets/{id}/close")
    public TicketResponse closeTicket(@PathVariable @Positive Long id) {
        return ticketService.closeTicket(id);
    }

    @PatchMapping("/tickets/{id}/assign")
    public TicketResponse assignTicket(
            @PathVariable @Positive Long id,
            @Valid @RequestBody AssignTicketRequest request
            ) {
        return ticketService.assignTicket(id, request);
    }
}
