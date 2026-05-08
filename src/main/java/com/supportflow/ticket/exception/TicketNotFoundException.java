package com.supportflow.ticket.exception;

public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException() {
        super("Ticket not found");
    }

    public TicketNotFoundException(Long id) {
        super("Ticket with id " + id + " not found");
    }
}
