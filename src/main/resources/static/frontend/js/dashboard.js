import { logout } from './auth.js';
import { requireAuth } from './storage.js';
import {
    assignTicket,
    closeTicket,
    createTicket,
    loadSlaBreachedTickets,
    loadTickets,
    resolveTicket,
    TICKET_STATUSES,
    updateTicketStatus
} from './tickets.js';
import { createComment, loadComments, renderComments } from './comments.js';
import { loadAgents, loadUsers, updateUserRole, updateUserStatus } from './users.js';

const state = {
    currentUser: null,
    agents: [],
    filters: {},
    showingSlaBreached: false
};

const elements = {};

initDashboard();

async function initDashboard() {
    state.currentUser = requireAuth();

    if (!state.currentUser) {
        return;
    }

    cacheElements();
    bindEvents();
    configurePageForRole();
    refreshTickets();

    if (state.currentUser.role === 'ADMIN') {
        await refreshUsers();
        await refreshAgents();
    }
    await refreshTickets();
}

function cacheElements() {
    elements.currentUser = document.getElementById('currentUser');
    elements.logoutButton = document.getElementById('logoutButton');
    elements.message = document.getElementById('message');

    elements.createTicketSection = document.getElementById('createTicketSection');
    elements.createTicketForm = document.getElementById('createTicketForm');
    elements.ticketTitle = document.getElementById('ticketTitle');
    elements.ticketDescription = document.getElementById('ticketDescription');
    elements.ticketPriority = document.getElementById('ticketPriority');

    elements.ticketsTitle = document.getElementById('ticketsTitle');
    elements.ticketsSubtitle = document.getElementById('ticketsSubtitle');
    elements.reloadTicketsButton = document.getElementById('reloadTicketsButton');
    elements.slaBreachedButton = document.getElementById('slaBreachedButton');
    elements.ticketFilters = document.getElementById('ticketFilters');
    elements.filterStatus = document.getElementById('filterStatus');
    elements.filterPriority = document.getElementById('filterPriority');
    elements.filterCreatedById = document.getElementById('filterCreatedById');
    elements.filterAssignedToId = document.getElementById('filterAssignedToId');
    elements.clearFiltersButton = document.getElementById('clearFiltersButton');
    elements.ticketsContainer = document.getElementById('ticketsContainer');

    elements.usersSection = document.getElementById('usersSection');
    elements.reloadUsersButton = document.getElementById('reloadUsersButton');
    elements.usersContainer = document.getElementById('usersContainer');
}

function bindEvents() {
    elements.logoutButton.addEventListener('click', logout);
    elements.reloadTicketsButton.addEventListener('click', () => {
        state.showingSlaBreached = false;
        refreshTickets();
    });

    elements.slaBreachedButton.addEventListener('click', async () => {
        state.showingSlaBreached = true;
        await refreshTickets();
    });

    elements.ticketFilters.addEventListener('submit', async (event) => {
        event.preventDefault();
        state.showingSlaBreached = false;
        state.filters = readFilters();
        await refreshTickets();
    });

    elements.clearFiltersButton.addEventListener('click', async () => {
        clearFilters();
        state.showingSlaBreached = false;
        state.filters = {};
        await refreshTickets();
    });

    elements.createTicketForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        await handleCreateTicket();
    });

    elements.reloadUsersButton.addEventListener('click', refreshUsers);
}

function configurePageForRole() {
    elements.currentUser.textContent = `${state.currentUser.name} · ${state.currentUser.email} · ${state.currentUser.role}`;

    if (state.currentUser.role === 'USER') {
        elements.createTicketSection.classList.remove('hidden');
        elements.ticketsTitle.textContent = 'Мои тикеты';
        elements.ticketsSubtitle.textContent = 'Обычный пользователь видит только свои заявки.';
    }

    if (state.currentUser.role === 'AGENT' || state.currentUser.role === 'ADMIN') {
        elements.ticketFilters.classList.remove('hidden');
        elements.slaBreachedButton.classList.remove('hidden');
        elements.ticketsTitle.textContent = 'Все тикеты';
        elements.ticketsSubtitle.textContent = 'AGENT и ADMIN видят общий список заявок.';
    }

    if (state.currentUser.role === 'ADMIN') {
        elements.usersSection.classList.remove('hidden');
    }
}

async function handleCreateTicket() {
    try {
        await createTicket(
            elements.ticketTitle.value.trim(),
            elements.ticketDescription.value.trim(),
            elements.ticketPriority.value
        );

        elements.createTicketForm.reset();
        elements.ticketPriority.value = 'MEDIUM';
        showMessage('Тикет создан.', 'success');
        await refreshTickets();
    } catch (error) {
        showMessage(error.message, 'error');
    }
}

async function refreshTickets() {
    try {
        elements.ticketsContainer.innerHTML = '<p class="muted">Загрузка...</p>';

        const tickets = state.showingSlaBreached
            ? await loadSlaBreachedTickets()
            : await loadTickets(state.currentUser, state.filters);

        renderTickets(tickets);
    } catch (error) {
        elements.ticketsContainer.innerHTML = '';
        showMessage(error.message, 'error');
    }
}

function renderTickets(tickets) {
    if (!tickets.length) {
        elements.ticketsContainer.innerHTML = '<p class="muted">Тикетов нет.</p>';
        return;
    }

    elements.ticketsContainer.innerHTML = tickets.map(renderTicketCard).join('');
    bindTicketActionEvents();
}

function renderTicketCard(ticket) {
    const canManage = canManageTicket(ticket);
    const canAssign = canAssignTicket(ticket);
    const statusOptions = TICKET_STATUSES.map(status => `
        <option value="${status}" ${ticket.status === status ? 'selected' : ''}>${status}</option>
    `).join('');

    return `
        <article class="ticket-card ${ticket.slaBreached ? 'sla-breached' : ''}" data-ticket-id="${ticket.id}">
            <div class="ticket-header">
                <div>
                    <h3 class="ticket-title">#${ticket.id} · ${escapeHtml(ticket.title)}</h3>
                    <p class="ticket-description">${escapeHtml(ticket.description)}</p>
                </div>
                <div class="actions-row">
                    <span class="badge">${ticket.status}</span>
                    <span class="badge">${ticket.priority}</span>
                    ${ticket.slaBreached ? '<span class="badge danger">SLA</span>' : ''}
                </div>
            </div>

            <div class="meta-grid">
                <div class="meta-item"><span class="meta-label">Создал</span>${escapeHtml(ticket.createdByName)} (${ticket.createById ?? '-'})</div>
                <div class="meta-item"><span class="meta-label">Назначен</span>${escapeHtml(ticket.assignedToName ?? 'Не назначен')} (${ticket.assignedToId ?? '-'})</div>
                <div class="meta-item"><span class="meta-label">Создан</span>${formatDate(ticket.createdAt)}</div>
                <div class="meta-item"><span class="meta-label">Resolved at</span>${formatDate(ticket.resolvedAt)}</div>
                <div class="meta-item"><span class="meta-label">First response deadline</span>${formatDate(ticket.firstResponseDeadline)}</div>
                <div class="meta-item"><span class="meta-label">Resolution deadline</span>${formatDate(ticket.resolutionDeadline)}</div>
                <div class="meta-item"><span class="meta-label">First responded at</span>${formatDate(ticket.firstRespondedAt)}</div>
                <div class="meta-item"><span class="meta-label">Updated</span>${formatDate(ticket.updatedAt)}</div>
            </div>

            <div class="ticket-actions">
                ${canManage ? `
                    <select class="inline-select status-select" data-ticket-id="${ticket.id}">
                        ${statusOptions}
                    </select>
                    <button class="button secondary small resolve-button" data-ticket-id="${ticket.id}" type="button">Resolve</button>
                    <button class="button secondary small close-button" data-ticket-id="${ticket.id}" type="button">Close</button>
                ` : ''}

                ${canAssign ? renderAssignControls(ticket) : ''}

                <button class="button secondary small comments-button" data-ticket-id="${ticket.id}" type="button">Комментарии</button>
            </div>

            <div id="comments-${ticket.id}" class="comments-box hidden"></div>
        </article>
    `;
}

function renderAssignControls(ticket) {
    if (state.currentUser.role === 'AGENT') {
        return `<button class="button secondary small assign-self-button" data-ticket-id="${ticket.id}" type="button">Назначить на себя</button>`;
    }

    if (state.currentUser.role === 'ADMIN') {
        const agentOptions = state.agents.map(agent => `
            <option value="${agent.id}" ${ticket.assignedToId === agent.id ? 'selected' : ''}>
                ${escapeHtml(agent.name)} · #${agent.id}
            </option>
        `).join('');

        return `
            <select class="inline-select agent-select" data-ticket-id="${ticket.id}">
                <option value="">Выбрать агента</option>
                ${agentOptions}
            </select>
            <button class="button secondary small assign-agent-button" data-ticket-id="${ticket.id}" type="button">Назначить</button>
        `;
    }

    return '';
}

function bindTicketActionEvents() {
    document.querySelectorAll('.status-select').forEach(select => {
        select.addEventListener('change', async (event) => {
            await handleTicketAction(() => updateTicketStatus(event.target.dataset.ticketId, event.target.value));
        });
    });

    document.querySelectorAll('.resolve-button').forEach(button => {
        button.addEventListener('click', async () => {
            await handleTicketAction(() => resolveTicket(button.dataset.ticketId));
        });
    });

    document.querySelectorAll('.close-button').forEach(button => {
        button.addEventListener('click', async () => {
            await handleTicketAction(() => closeTicket(button.dataset.ticketId));
        });
    });

    document.querySelectorAll('.assign-self-button').forEach(button => {
        button.addEventListener('click', async () => {
            await handleTicketAction(() => assignTicket(button.dataset.ticketId, state.currentUser.id));
        });
    });

    document.querySelectorAll('.assign-agent-button').forEach(button => {
        button.addEventListener('click', async () => {
            const select = document.querySelector(`.agent-select[data-ticket-id="${button.dataset.ticketId}"]`);
            const agentId = Number(select.value);

            if (!agentId) {
                showMessage('Выбери агента.', 'error');
                return;
            }

            await handleTicketAction(() => assignTicket(button.dataset.ticketId, agentId));
        });
    });

    document.querySelectorAll('.comments-button').forEach(button => {
        button.addEventListener('click', async () => {
            await toggleComments(button.dataset.ticketId);
        });
    });
}

async function handleTicketAction(action) {
    try {
        await action();
        showMessage('Изменение сохранено.', 'success');
        await refreshTickets();
    } catch (error) {
        showMessage(error.message, 'error');
        await refreshTickets();
    }
}

async function toggleComments(ticketId) {
    const box = document.getElementById(`comments-${ticketId}`);

    if (!box.classList.contains('hidden')) {
        box.classList.add('hidden');
        box.innerHTML = '';
        return;
    }

    try {
        box.classList.remove('hidden');
        box.innerHTML = '<p class="muted">Загрузка комментариев...</p>';
        const comments = await loadComments(ticketId);

        box.innerHTML = `
            <div class="comment-list">${renderComments(comments)}</div>
            <form class="comment-form" data-ticket-id="${ticketId}">
                <textarea rows="3" placeholder="Комментарий" required></textarea>
                <button class="button primary small" type="submit">Добавить комментарий</button>
            </form>
        `;

        const form = box.querySelector('.comment-form');
        form.addEventListener('submit', async (event) => {
            event.preventDefault();
            const textarea = form.querySelector('textarea');
            const message = textarea.value.trim();

            if (!message) {
                return;
            }

            try {
                await createComment(ticketId, message);
                await toggleComments(ticketId);
                await toggleComments(ticketId);
            } catch (error) {
                showMessage(error.message, 'error');
            }
        });
    } catch (error) {
        box.innerHTML = '';
        box.classList.add('hidden');
        showMessage(error.message, 'error');
    }
}

async function refreshUsers() {
    if (state.currentUser.role !== 'ADMIN') {
        return;
    }

    try {
        elements.usersContainer.innerHTML = '<p class="muted">Загрузка...</p>';
        const users = await loadUsers();
        renderUsers(users);
    } catch (error) {
        showMessage(error.message, 'error');
    }
}

async function refreshAgents() {
    if (state.currentUser.role !== 'ADMIN') {
        return;
    }

    try {
        state.agents = await loadAgents();
    } catch (_error) {
        state.agents = [];
    }
}

function renderUsers(users) {
    if (!users.length) {
        elements.usersContainer.innerHTML = '<p class="muted">Пользователей нет.</p>';
        return;
    }

    elements.usersContainer.innerHTML = `
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Имя</th>
                <th>Email</th>
                <th>Роль</th>
                <th>Статус</th>
                <th>Создан</th>
            </tr>
            </thead>
            <tbody>
            ${users.map(user => `
                <tr data-user-id="${user.id}">
                    <td>${user.id}</td>
                    <td>${escapeHtml(user.name)}</td>
                    <td>${escapeHtml(user.email)}</td>
                    <td>
                        <select class="inline-select user-role-select" data-user-id="${user.id}">
                            ${['USER', 'AGENT', 'ADMIN'].map(role => `
                                <option value="${role}" ${user.role === role ? 'selected' : ''}>${role}</option>
                            `).join('')}
                        </select>
                    </td>
                    <td>
                        <select class="inline-select user-status-select" data-user-id="${user.id}">
                            ${['ACTIVE', 'BLOCKED'].map(status => `
                                <option value="${status}" ${user.status === status ? 'selected' : ''}>${status}</option>
                            `).join('')}
                        </select>
                    </td>
                    <td>${formatDate(user.createdAt)}</td>
                </tr>
            `).join('')}
            </tbody>
        </table>
    `;

    bindUserActionEvents();
}

function bindUserActionEvents() {
    document.querySelectorAll('.user-role-select').forEach(select => {
        select.addEventListener('change', async (event) => {
            try {
                await updateUserRole(event.target.dataset.userId, event.target.value);
                showMessage('Роль пользователя обновлена.', 'success');
                await refreshAgents();
                await refreshUsers();
            } catch (error) {
                showMessage(error.message, 'error');
                await refreshUsers();
            }
        });
    });

    document.querySelectorAll('.user-status-select').forEach(select => {
        select.addEventListener('change', async (event) => {
            try {
                await updateUserStatus(event.target.dataset.userId, event.target.value);
                showMessage('Статус пользователя обновлён.', 'success');
                await refreshAgents();
                await refreshUsers();
            } catch (error) {
                showMessage(error.message, 'error');
                await refreshUsers();
            }
        });
    });
}

function canManageTicket(ticket) {
    if (state.currentUser.role === 'ADMIN') {
        return true;
    }

    return state.currentUser.role === 'AGENT' && ticket.assignedToId === state.currentUser.id;
}

function canAssignTicket(ticket) {
    if (state.currentUser.role === 'ADMIN') {
        return true;
    }

    if (state.currentUser.role !== 'AGENT') {
        return false;
    }

    return ticket.assignedToId === null || ticket.assignedToId === state.currentUser.id;
}

function readFilters() {
    return {
        status: elements.filterStatus.value,
        priority: elements.filterPriority.value,
        createdById: elements.filterCreatedById.value,
        assignedToId: elements.filterAssignedToId.value
    };
}

function clearFilters() {
    elements.filterStatus.value = '';
    elements.filterPriority.value = '';
    elements.filterCreatedById.value = '';
    elements.filterAssignedToId.value = '';
}

function showMessage(text, type = 'error') {
    elements.message.textContent = text;
    elements.message.className = `message ${type}`;

    window.setTimeout(() => {
        elements.message.classList.add('hidden');
    }, 5000);
}

function formatDate(value) {
    if (!value) {
        return '-';
    }

    return new Date(value).toLocaleString('ru-RU');
}

function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}
