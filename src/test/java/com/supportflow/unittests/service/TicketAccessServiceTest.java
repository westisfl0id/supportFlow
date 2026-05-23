package com.supportflow.unittests.service;

import com.supportflow.exception.ForbiddenActionException;
import com.supportflow.ticket.entity.TicketEntity;
import com.supportflow.ticket.service.TicketAccessService;
import com.supportflow.user.entity.UserEntity;
import com.supportflow.user.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketAccessServiceTest {

    private final TicketAccessService ticketAccessService = new TicketAccessService();

    @Test
    void checkCanViewTicket_shouldAllowOwner() {
        UserEntity owner = user(1L, UserRole.USER);
        TicketEntity ticket = ticket(owner, null);

        assertDoesNotThrow(() ->
                ticketAccessService.checkCanViewTicket(owner, ticket)
        );
    }

    @Test
    void checkCanViewTicket_shouldRejectOtherUser() {
        UserEntity owner = user(1L, UserRole.USER);
        UserEntity otherUser = user(2L, UserRole.USER);
        TicketEntity ticket = ticket(owner, null);

        assertThrows(
                ForbiddenActionException.class,
                () -> ticketAccessService.checkCanViewTicket(otherUser, ticket)
        );
    }

    @Test
    void checkCanViewTicket_shouldAllowAgent() {
        UserEntity owner = user(1L, UserRole.USER);
        UserEntity agent = user(2L, UserRole.AGENT);
        TicketEntity ticket = ticket(owner, null);

        assertDoesNotThrow(() ->
                ticketAccessService.checkCanViewTicket(agent, ticket)
        );
    }

    @Test
    void checkCanViewTicket_shouldAllowAdmin() {
        UserEntity owner = user(1L, UserRole.USER);
        UserEntity admin = user(2L, UserRole.ADMIN);
        TicketEntity ticket = ticket(owner, null);

        assertDoesNotThrow(() ->
                ticketAccessService.checkCanViewTicket(admin, ticket)
        );
    }

    @Test
    void checkCanCommentTicket_shouldAllowOwner() {
        UserEntity owner = user(1L, UserRole.USER);
        TicketEntity ticket = ticket(owner, null);

        assertDoesNotThrow(() ->
                ticketAccessService.checkCanCommentTicket(owner, ticket)
        );
    }

    @Test
    void checkCanCommentTicket_shouldRejectOtherUser() {
        UserEntity owner = user(1L, UserRole.USER);
        UserEntity otherUser = user(2L, UserRole.USER);
        TicketEntity ticket = ticket(owner, null);

        assertThrows(
                ForbiddenActionException.class,
                () -> ticketAccessService.checkCanCommentTicket(otherUser, ticket)
        );
    }

    @Test
    void checkCanCommentTicket_shouldAllowAnyAgent() {
        UserEntity owner = user(1L, UserRole.USER);
        UserEntity agent = user(2L, UserRole.AGENT);
        TicketEntity ticket = ticket(owner, null);

        assertDoesNotThrow(() ->
                ticketAccessService.checkCanCommentTicket(agent, ticket)
        );
    }

    @Test
    void checkCanManageTicket_shouldAllowAdmin() {
        UserEntity owner = user(1L, UserRole.USER);
        UserEntity admin = user(2L, UserRole.ADMIN);
        TicketEntity ticket = ticket(owner, null);

        assertDoesNotThrow(() ->
                ticketAccessService.checkCanManageTicket(admin, ticket)
        );
    }

    @Test
    void checkCanManageTicket_shouldAllowAssignedAgent() {
        UserEntity owner = user(1L, UserRole.USER);
        UserEntity agent = user(2L, UserRole.AGENT);
        TicketEntity ticket = ticket(owner, agent);

        assertDoesNotThrow(() ->
                ticketAccessService.checkCanManageTicket(agent, ticket)
        );
    }

    @Test
    void checkCanManageTicket_shouldRejectUnassignedAgent() {
        UserEntity owner = user(1L, UserRole.USER);
        UserEntity agent = user(2L, UserRole.AGENT);
        TicketEntity ticket = ticket(owner, null);

        assertThrows(
                ForbiddenActionException.class,
                () -> ticketAccessService.checkCanManageTicket(agent, ticket)
        );
    }

    @Test
    void checkCanManageTicket_shouldRejectAnotherAgent() {
        UserEntity owner = user(1L, UserRole.USER);
        UserEntity assignedAgent = user(2L, UserRole.AGENT);
        UserEntity anotherAgent = user(3L, UserRole.AGENT);
        TicketEntity ticket = ticket(owner, assignedAgent);

        assertThrows(
                ForbiddenActionException.class,
                () -> ticketAccessService.checkCanManageTicket(anotherAgent, ticket)
        );
    }

    @Test
    void checkCanAssignTicket_shouldAllowAdminToAssignAnyAgent() {
        UserEntity owner = user(1L, UserRole.USER);
        UserEntity admin = user(2L, UserRole.ADMIN);
        UserEntity targetAgent = user(3L, UserRole.AGENT);
        TicketEntity ticket = ticket(owner, null);

        assertDoesNotThrow(() ->
                ticketAccessService.checkCanAssignTicket(admin, ticket, targetAgent)
        );
    }

    @Test
    void checkCanAssignTicket_shouldAllowAgentToAssignUnassignedTicketToHimself() {
        UserEntity owner = user(1L, UserRole.USER);
        UserEntity agent = user(2L, UserRole.AGENT);
        TicketEntity ticket = ticket(owner, null);

        assertDoesNotThrow(() ->
                ticketAccessService.checkCanAssignTicket(agent, ticket, agent)
        );
    }

    @Test
    void checkCanAssignTicket_shouldRejectAgentAssigningTicketToAnotherAgent() {
        UserEntity owner = user(1L, UserRole.USER);
        UserEntity agent = user(2L, UserRole.AGENT);
        UserEntity anotherAgent = user(3L, UserRole.AGENT);
        TicketEntity ticket = ticket(owner, null);

        assertThrows(
                ForbiddenActionException.class,
                () -> ticketAccessService.checkCanAssignTicket(agent, ticket, anotherAgent)
        );
    }

    @Test
    void checkCanAssignTicket_shouldRejectAgentTakingAssignedTicketFromAnotherAgent() {
        UserEntity owner = user(1L, UserRole.USER);
        UserEntity assignedAgent = user(2L, UserRole.AGENT);
        UserEntity anotherAgent = user(3L, UserRole.AGENT);
        TicketEntity ticket = ticket(owner, assignedAgent);

        assertThrows(
                ForbiddenActionException.class,
                () -> ticketAccessService.checkCanAssignTicket(anotherAgent, ticket, anotherAgent)
        );
    }

    private UserEntity user(Long id, UserRole role) {
        return UserEntity.builder()
                .id(id)
                .role(role)
                .build();
    }

    private TicketEntity ticket(UserEntity owner, UserEntity assignedTo) {
        return TicketEntity.builder()
                .createdBy(owner)
                .assignedTo(assignedTo)
                .build();
    }
}
