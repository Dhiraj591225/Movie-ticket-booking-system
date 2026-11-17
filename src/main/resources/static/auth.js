function getCookieValue(name) {
    const entry = document.cookie.split('; ').find(row => row.startsWith(name + '='));
    return entry ? decodeURIComponent(entry.split('=').slice(1).join('=')) : null;
}

async function refreshCsrfToken() {
    await fetch('/auth/csrf', { credentials: 'same-origin' });
    return getCookieValue('XSRF-TOKEN');
}

async function cineBookFetch(url, options = {}) {
    const method = (options.method || 'GET').toUpperCase();
    const headers = new Headers(options.headers || {});
    if (!['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(method)) {
        let token = getCookieValue('XSRF-TOKEN');
        if (!token) token = await refreshCsrfToken();
        if (token) headers.set('X-XSRF-TOKEN', token);
    }
    return fetch(url, { ...options, headers, credentials: 'same-origin' });
}

function redirectAfterAuth() {
    const params = new URLSearchParams(window.location.search);
    const requested = params.get('redirect') || 'index.html';
    const safePath = (requested === 'index.html' || requested === '/index.html') ? requested : 'index.html';
    const movieId = params.get('movieId');
    const target = new URL(safePath, window.location.origin);
    if (movieId && target.pathname.endsWith('/index.html')) target.searchParams.set('movieId', movieId);
    window.location.assign(target.pathname + target.search + target.hash);
}

function showAuthMessage(message) {
    const el = document.getElementById('authMessage');
    if (el) el.textContent = message || '';
}

async function initializeAuthPage() {
    const params = new URLSearchParams(window.location.search);
    const redirect = params.get('redirect') || 'index.html';
    const movieId = params.get('movieId');
    const query = new URLSearchParams({ redirect });
    if (movieId) query.set('movieId', movieId);
    const registerLink = document.getElementById('registerLink');
    const loginLink = document.getElementById('loginLink');
    if (registerLink) registerLink.href = 'register.html?' + query.toString();
    if (loginLink) loginLink.href = 'login.html?' + query.toString();

    const loginForm = document.getElementById('loginForm');
    if (loginForm) loginForm.addEventListener('submit', async event => {
        event.preventDefault(); showAuthMessage('Signing in...');
        try {
            const response = await cineBookFetch('/auth/login', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ email: loginForm.email.value, password: loginForm.password.value }) });
            const data = await response.json();
            if (!response.ok) throw new Error(data.message || 'Login failed.');
            redirectAfterAuth();
        } catch (error) { showAuthMessage(error.message); }
    });

    const registerForm = document.getElementById('registerForm');
    if (registerForm) registerForm.addEventListener('submit', async event => {
        event.preventDefault(); showAuthMessage('Creating your account...');
        try {
            const response = await cineBookFetch('/auth/register', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ name: registerForm.name.value, email: registerForm.email.value, password: registerForm.password.value }) });
            const data = await response.json();
            if (!response.ok) throw new Error(data.message || 'Registration failed.');
            const next = new URLSearchParams({ redirect });
            if (movieId) next.set('movieId', movieId);
            window.location.assign('login.html?' + next.toString() + '&registered=1');
        } catch (error) { showAuthMessage(error.message); }
    });
    if (params.get('registered') === '1') showAuthMessage('Account created. Please log in.');
}

document.addEventListener('DOMContentLoaded', initializeAuthPage);
