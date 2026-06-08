import { post } from './api.js';
import { clearAuth, saveAuth } from './storage.js';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PASSWORD_MIN_LENGTH = 6;
const PASSWORD_MAX_LENGTH = 72;

function showMessage(text, type = 'error') {
    const message = document.getElementById('message');

    if (!message) {
        return;
    }

    message.textContent = text;
    message.className = `message ${type}`;
    message.classList.remove('hidden');
}

function hideMessage() {
    const message = document.getElementById('message');

    if (!message) {
        return;
    }

    message.textContent = '';
    message.className = 'message hidden';
}

function getInputValue(id) {
    const input = document.getElementById(id);
    return input ? input.value.trim() : '';
}

function getPasswordValue(id = 'password') {
    const input = document.getElementById(id);
    return input ? input.value : '';
}

function markFieldError(id) {
    const input = document.getElementById(id);

    if (input) {
        input.classList.add('input-error');
        input.focus();
    }
}

function clearFieldErrors() {
    document.querySelectorAll('input').forEach(input => {
        input.classList.remove('input-error');
    });
}

function validateEmail(email) {
    if (!email) {
        return {
            field: 'email',
            message: 'Введите email.'
        };
    }

    if (email.length > 120) {
        return {
            field: 'email',
            message: 'Email не должен быть длиннее 120 символов.'
        };
    }

    if (!EMAIL_PATTERN.test(email)) {
        return {
            field: 'email',
            message: 'Введите корректный email.'
        };
    }

    return null;
}

function validatePassword(password) {
    if (!password) {
        return {
            field: 'password',
            message: 'Введите пароль.'
        };
    }

    if (password.length < PASSWORD_MIN_LENGTH) {
        return {
            field: 'password',
            message: `Пароль должен содержать минимум ${PASSWORD_MIN_LENGTH} символов.`
        };
    }

    if (password.length > PASSWORD_MAX_LENGTH) {
        return {
            field: 'password',
            message: `Пароль не должен быть длиннее ${PASSWORD_MAX_LENGTH} символов.`
        };
    }

    return null;
}

function validateName(name) {
    if (!name) {
        return {
            field: 'name',
            message: 'Введите имя.'
        };
    }

    if (name.length < 2) {
        return {
            field: 'name',
            message: 'Имя должно содержать минимум 2 символа.'
        };
    }

    if (name.length > 80) {
        return {
            field: 'name',
            message: 'Имя не должно быть длиннее 80 символов.'
        };
    }

    return null;
}

function validateLoginForm(email, password) {
    return validateEmail(email) || validatePassword(password);
}

function validateRegisterForm(name, email, password, confirmPassword) {
    const validationError = validateName(name) || validateEmail(email) || validatePassword(password);

    if (validationError) {
        return validationError;
    }

    if (!confirmPassword) {
        return {
            field: 'confirmPassword',
            message: 'Повторите пароль.'
        };
    }

    if (password !== confirmPassword) {
        return {
            field: 'confirmPassword',
            message: 'Пароли не совпадают.'
        };
    }

    return null;
}

export async function login(email, password) {
    const response = await post('/auth/login', { email, password });
    saveAuth(response);
    return response;
}

export async function register(name, email, password) {
    return post('/auth/register', { name, email, password });
}

export function logout() {
    clearAuth();
    window.location.href = 'login.html';
}

function getAuthErrorMessage(error) {
    const message = error?.message || '';

    if (message.toLowerCase().includes('invalid email or password')) {
        return 'Неверный email или пароль.';
    }

    if (message.toLowerCase().includes('bad credentials')) {
        return 'Неверный email или пароль.';
    }

    if (message.toLowerCase().includes('failed to fetch')) {
        return 'Не удалось подключиться к серверу.';
    }

    if (message.toLowerCase().includes('user is blocked')) {
        return 'Пользователь заблокирован.';
    }

    return message || 'Не удалось войти.';
}

function getRegisterErrorMessage(error) {
    const message = error?.message || '';

    if (message.toLowerCase().includes('email already exists')) {
        return 'Пользователь с таким email уже существует.';
    }

    if (message.toLowerCase().includes('already exists')) {
        return 'Пользователь с такими данными уже существует.';
    }

    if (message.toLowerCase().includes('failed to fetch')) {
        return 'Не удалось подключиться к серверу.';
    }

    return message || 'Не удалось зарегистрироваться.';
}

export function initLoginPage() {
    const form = document.getElementById('loginForm');

    if (new URLSearchParams(window.location.search).get('registered') === '1') {
        showMessage('Регистрация прошла успешно. Теперь войдите в аккаунт.', 'success');
    } else {
        hideMessage();
    }

    form.addEventListener('submit', async (event) => {
        event.preventDefault();

        clearFieldErrors();

        const email = getInputValue('email');
        const password = getPasswordValue('password');
        const validationError = validateLoginForm(email, password);

        if (validationError) {
            showMessage(validationError.message, 'error');
            markFieldError(validationError.field);
            return;
        }

        try {
            await login(email, password);
            window.location.href = 'dashboard.html';
        } catch (error) {
            showMessage(getAuthErrorMessage(error), 'error');
        }
    });
}

export function initRegisterPage() {
    const form = document.getElementById('registerForm');

    hideMessage();

    form.addEventListener('submit', async (event) => {
        event.preventDefault();

        clearFieldErrors();

        const name = getInputValue('name');
        const email = getInputValue('email');
        const password = getPasswordValue('password');
        const confirmPassword = getPasswordValue('confirmPassword');
        const validationError = validateRegisterForm(name, email, password, confirmPassword);

        if (validationError) {
            showMessage(validationError.message, 'error');
            markFieldError(validationError.field);
            return;
        }

        try {
            await register(name, email, password);
            clearAuth();
            window.location.href = 'login.html?registered=1';
        } catch (error) {
            showMessage(getRegisterErrorMessage(error), 'error');
        }
    });
}