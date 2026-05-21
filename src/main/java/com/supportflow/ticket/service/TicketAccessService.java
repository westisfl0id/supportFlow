package com.supportflow.ticket.service;

import com.supportflow.exception.ForbiddenActionException;
import com.supportflow.ticket.entity.TicketEntity;
import com.supportflow.ticket.enums.TicketStatus;
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

    public void checkCanManageTicket(UserEntity user, TicketEntity ticket) {
        if (user.getRole() == UserRole.ADMIN) {
            return;
        }

        if (user.getRole() == UserRole.AGENT
            && ticket.getAssignedTo() != null
            && ticket.getAssignedTo().getId().equals(user.getId())) {
            return;
        }

        throw new ForbiddenActionException("Only admin or assigned agent can manage this ticket");
    }

    public void checkCanAssignTicket(UserEntity user, TicketEntity ticket, UserEntity targetAgent) {
        if (user.getRole() == UserRole.ADMIN) {
            return;
        }

        if (user.getRole() == UserRole.AGENT) {
            boolean agentAssignsToHimself = targetAgent.getId().equals(user.getId());
            boolean ticketIsNotAssigned = ticket.getAssignedTo() == null;
            boolean ticketAlreadyAssignedToThisAgent = ticket.getAssignedTo() != null
                    && ticket.getAssignedTo().getId().equals(user.getId());

            if (agentAssignsToHimself && (ticketIsNotAssigned || ticketAlreadyAssignedToThisAgent)) {
                return;
            }
        }
        throw new ForbiddenActionException("Agent can assign only unassigned tickets to himself");
    }

    public void checkCanReopenTicket(UserEntity user, TicketEntity ticket) {
        if (user.getRole() == UserRole.ADMIN) {
            return;
        }

        if (user.getRole() == UserRole.AGENT
                && ticket.getAssignedTo() != null
                && ticket.getAssignedTo().getId().equals(user.getId())) {
            return;
        }

        if (ticket.getCreatedBy().getId().equals(user.getId())) {
            return;
        }

        throw new ForbiddenActionException("Only ticket owner, assigned agent or admin can reopen ticket");
    }
}
