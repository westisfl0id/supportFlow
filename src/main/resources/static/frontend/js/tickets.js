import { get, patch, post } from './api.js';

export const TICKET_STATUSES = [
    'NEW',
    'OPEN',
    'IN_PROGRESS',
    'WAITING',
    'RESOLVED',
    'CLOSED'
];

export async function loadTickets(currentUser, filters = {}, page = 0, size = 10) {
    const params = new URLSearchParams();

    params.append('page', page);
    params.append('size', size);

    if (currentUser.role === 'USER') {
        return get(`/tickets/my?${params.toString()}`);
    }

    if (filters.status) params.append('status', filters.status);
    if (filters.priority) params.append('priority', filters.priority);
    if (filters.createdById) params.append('createdById', filters.createdById);
    if (filters.assignedToId) params.append('assignedToId', filters.assignedToId);

    return get(`/tickets?${params.toString()}`);
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
