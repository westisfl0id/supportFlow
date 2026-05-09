package com.supportflow.ticket.exception;

public class TicketAlreadyClosedException extends RuntimeException {
    public TicketAlreadyClosedException(Long ticketId) {
        super("Ticket with id " + ticketId + " is already closed");
    }
}
