package com.supportflow.ticket.exception;

import com.supportflow.ticket.enums.TicketStatus;

public class InvalidTicketStatusTransitionException extends RuntimeException {
    public InvalidTicketStatusTransitionException(TicketStatus currentStatus, TicketStatus targetStatus) {
        super("Invalid ticket status transition: " + currentStatus + " -> " + targetStatus);
    }
}
