import { get, patch } from './api.js';

export async function loadUsers() {
    return get('/users');
}

export async function loadAgents() {
    const users = await loadUsers();
    return users.filter(user => user.role === 'AGENT' && user.status === 'ACTIVE');
}

export async function updateUserRole(userId, role) {
    return patch(`/users/${userId}/role`, { role });
}

export async function updateUserStatus(userId, status) {
    return patch(`/users/${userId}/status`, { status });
}
