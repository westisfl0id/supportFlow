package com.supportflow.ticket.specification;

import com.supportflow.ticket.entity.TicketEntity;
import com.supportflow.ticket.enums.TicketCategory;
import com.supportflow.ticket.enums.TicketPriority;
import com.supportflow.ticket.enums.TicketStatus;
import org.springframework.data.jpa.domain.Specification;

public class TicketSpecification {
    public static Specification<TicketEntity> hasStatus(TicketStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<TicketEntity> hasPriority (TicketPriority priority) {
        return (root, query, cb) ->
                priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    public static Specification<TicketEntity> createdBy(Long createdById) {
        return (root, query, cb) ->
                createdById == null ? null : cb.equal(root.get("createdBy").get("id"), createdById);
    }

    public static Specification<TicketEntity> assignedTo(Long assignedToId) {
        return (root, query, cb) ->
                assignedToId == null ? null : cb.equal(root.get("assignedTo").get("id"), assignedToId);
    }

    public static Specification<TicketEntity> hasCategory(TicketCategory category) {
        return (root, query, criteriaBuilder) -> {
            if (category == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("category"), category);
        };
    }

    public static Specification<TicketEntity> hasCreatedById(Long createdById) {
        return (root, query, criteriaBuilder) -> {
            if (createdById == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("createdBy").get("id"), createdById);
        };
    }

    public static Specification<TicketEntity> hasAssignedToId(Long assignedToId) {
        return (root, query, criteriaBuilder) -> {
            if (assignedToId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("assignedTo").get("id"), assignedToId);
        };
    }
}


