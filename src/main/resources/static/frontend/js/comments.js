import { get, post } from './api.js';

export async function loadComments(ticketId) {
    return get(`/tickets/${ticketId}/comments`);
}

export async function createComment(ticketId, message) {
    return post(`/tickets/${ticketId}/comments`, { message });
}

export function renderComments(comments) {
    if (!comments.length) {
        return '<p class="muted">Комментариев пока нет.</p>';
    }

    return comments.map(comment => `
        <article class="comment-item">
            <div class="comment-meta">
                ${escapeHtml(comment.createdByName)} · ${comment.createdByRole} · ${formatDate(comment.createdAt)}
            </div>
            <div>${escapeHtml(comment.message)}</div>
        </article>
    `).join('');
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
