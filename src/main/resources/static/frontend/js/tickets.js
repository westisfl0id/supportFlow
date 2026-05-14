import { get, patch, post } from './api.js';

export const TICKET_STATUSES = [
    'NEW',
    'OPEN',
    'IN_PROGRESS',
    'WAITING',
    'RESOLVED',
    'CLOSED'
];

export async function loadTickets(currentUser, filters = {}) {
    if (currentUser.role === 'USER') {
        return get('/tickets/my');
    }

    const hasFilters = Object.values(filters).some(value => value !== undefined && value !== null && value !== '');

    if (hasFilters) {
        return get('/tickets/search', filters);
    }

    return get('/tickets');
}

export async function loadSlaBreachedTickets() {
    return get('/tickets/sla/breached');
}

export async function createTicket(title, description, priority) {
    return post('/tickets', {
        title,
        description,
        priority
    });
}

export async function updateTicketStatus(ticketId, status) {
    return patch(`/tickets/${ticketId}/status`, { status });
}

export async function assignTicket(ticketId, agentId) {
    return patch(`/tickets/${ticketId}/assign`, { agentId });
}

export async function resolveTicket(ticketId) {
    return patch(`/tickets/${ticketId}/resolve`);
}

export async function closeTicket(ticketId) {
    return patch(`/tickets/${ticketId}/close`);
}
