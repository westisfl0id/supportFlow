import { post } from './api.js';
import { clearAuth, saveAuth } from './storage.js';

function setMessage(text, type = 'error') {
    const message = document.getElementById('message');

    if (!message) {
        return;
    }

    message.textContent = text;
    message.className = `message ${type}`;
}

function getInputValue(id) {
    return document.getElementById(id).value.trim();
}

export async function login(email, password) {
    const response = await post('/auth/login', { email, password });
    saveAuth(response);
    return response;
}

export async function register(name, email, password) {
    const response = await post('/auth/register', { name, email, password });
    saveAuth(response);
    return response;
}

export function logout() {
    clearAuth();
    window.location.href = 'login.html';
}

export function initLoginPage() {
    const form = document.getElementById('loginForm');

    form.addEventListener('submit', async (event) => {
        event.preventDefault();

        try {
            await login(
                getInputValue('email'),
                getInputValue('password')
            );
            window.location.href = 'dashboard.html';
        } catch (error) {
            setMessage(error.message || 'Login failed');
        }
    });
}

export function initRegisterPage() {
    const form = document.getElementById('registerForm');

    form.addEventListener('submit', async (event) => {
        event.preventDefault();

        try {
            await register(
                getInputValue('name'),
                getInputValue('email'),
                getInputValue('password')
            );
            window.location.href = 'dashboard.html';
        } catch (error) {
            setMessage(error.message || 'Registration failed');
        }
    });
}
