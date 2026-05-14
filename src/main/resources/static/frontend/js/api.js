import { clearAuth, getToken } from './storage.js';

export const API_BASE_URL = '';

function buildUrl(path, params = {}) {
    const baseUrl = API_BASE_URL || window.location.origin;

    const url = new URL(path, baseUrl);

    Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
            url.searchParams.append(key, value);
        }
    });

    return url.toString();
}

function buildHeaders(body) {
    const headers = new Headers();
    const token = getToken();

    if (body !== undefined) {
        headers.set('Content-Type', 'application/json');
    }

    if (token) {
        headers.set('Authorization', `Bearer ${token}`);
    }

    return headers;
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

    if (Array.isArray(payload.errors)) {
        return payload.errors
            .map(error => error.message || `${error.field}: invalid value`)
            .join('; ');
    }

    if (Array.isArray(payload.fieldErrors)) {
        return payload.fieldErrors
            .map(error => error.message || `${error.field}: invalid value`)
            .join('; ');
    }

    return fallback;
}

export async function request(path, options = {}) {
    const { method = 'GET', body, params } = options;

    const response = await fetch(buildUrl(path, params), {
        method,
        headers: buildHeaders(body),
        body: body === undefined ? undefined : JSON.stringify(body)
    });

    const payload = await parseResponse(response);

    if (!response.ok) {
        if (response.status === 401) {
            clearAuth();
            if (!window.location.pathname.endsWith('/login.html')) {
                window.location.href = 'login.html';
            }
        }

        const message = extractErrorMessage(payload, `HTTP ${response.status}`);
        throw new Error(message);
    }

    return payload;
}

export function get(path, params) {
    return request(path, { method: 'GET', params });
}

export function post(path, body) {
    return request(path, { method: 'POST', body });
}

export function patch(path, body) {
    return request(path, { method: 'PATCH', body });
}
