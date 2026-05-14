const TOKEN_KEY = 'supportflow.token';
const USER_KEY = 'supportflow.user';

export function saveAuth(authResponse) {
    localStorage.setItem(TOKEN_KEY, authResponse.token);
    localStorage.setItem(USER_KEY, JSON.stringify({
        id: authResponse.userId,
        name: authResponse.name,
        email: authResponse.email,
        role: authResponse.role
    }));
}

export function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

export function getCurrentUser() {
    const rawUser = localStorage.getItem(USER_KEY);

    if (!rawUser) {
        return null;
    }

    try {
        return JSON.parse(rawUser);
    } catch (_error) {
        clearAuth();
        return null;
    }
}

export function isAuthenticated() {
    return Boolean(getToken());
}

export function clearAuth() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
}

export function requireAuth() {
    if (!isAuthenticated()) {
        window.location.href = 'login.html';
        return null;
    }

    return getCurrentUser();
}
