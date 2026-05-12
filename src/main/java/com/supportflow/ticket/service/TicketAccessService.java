package com.supportflow.ticket.service;

import com.supportflow.exception.ForbiddenActionException;
import com.supportflow.ticket.entity.TicketEntity;
import com.supportflow.user.entity.UserEntity;
import com.supportflow.user.enums.UserRole;
import org.springframework.stereotype.Service;

@Service
public class TicketAccessService {
    public void checkCanViewTicket(UserEntity user, TicketEntity ticket) {
        if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.AGENT) {
            return;
        }

        if (ticket.getCreatedBy().getId().equals(user.getId())) {
            return;
        }

        throw new ForbiddenActionException("You don`t have access to this ticket");
    }

    public void checkCanCommentTicket(UserEntity user, TicketEntity ticket) {
        if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.AGENT) {
            return;
        }

        if (ticket.getCreatedBy().getId().equals(user.getId())) {
            return;
        }

        throw new ForbiddenActionException("You can`t comment this ticket");
    }
}
