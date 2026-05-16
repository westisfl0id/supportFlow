import { getToken } from './storage.js';
import { get } from './api.js';

export async function loadTicketAttachments(ticketId) {
    return get(`/tickets/${ticketId}/attachments`);
}

export async function uploadTicketAttachments(ticketId, files) {
    const formData = new FormData();
    Array.from(files).forEach(file => {
        formData.append('files', file);
    });

    const headers = new Headers();
    const token = getToken();

    if (token) {
        headers.set('Authorization', `Bearer ${token}`);
    }

    const response = await fetch(`/tickets/${ticketId}/attachments`, {
        method: 'POST',
        headers,
        body: formData
    });

    const payload = await parseResponse(response);

    if (!response.ok) {
        throw new Error(extractErrorMessage(payload, `HTTP ${response.status}`));
    }

    return payload;
}

export async function downloadAttachment(attachmentId, filename) {
    const headers = new Headers();
    const token = getToken();

    if (token) {
        headers.set('Authorization', `Bearer ${token}`);
    }

    const response = await fetch(`/attachments/${attachmentId}/download`, {
        method: 'GET',
        headers
    });

    if (!response.ok) {
        const payload = await parseResponse(response);
        throw new Error(extractErrorMessage(payload, `HTTP ${response.status}`));
    }

    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);

    const link = document.createElement('a');
    link.href = url;
    link.download = filename || 'attachment';
    document.body.appendChild(link);
    link.click();

    link.remove();
    window.URL.revokeObjectURL(url);
}

async function parseResponse(response) {
    const contentType = response.headers.get('content-type') || '';

    if (response.status === 204) {
        return null;
    }

    if (contentType.includes('application/json')) {
        return response.json();
    }

    return response.text();
}

function extractErrorMessage(payload, fallback) {
    if (!payload) {
        return fallback;
    }

    if (typeof payload === 'string') {
        return payload;
    }

    if (payload.message) {
        return payload.message;
    }

    return fallback;
}