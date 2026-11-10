const Aurelia = (() => {
    let csrfToken;
    async function csrf() {
        if (csrfToken) return csrfToken;
        const response = await fetch('/api/auth/csrf', {credentials: 'same-origin'});
        if (!response.ok) throw new Error('Unable to initialize secure session.');
        const data = await response.json();
        csrfToken = data.token;
        return csrfToken;
    }
    async function request(url, options = {}) {
        const method = (options.method || 'GET').toUpperCase();
        const headers = {...(options.headers || {})};
        if (method !== 'GET' && method !== 'HEAD') headers['X-XSRF-TOKEN'] = await csrf();
        const response = await fetch(url, {...options, headers, credentials: 'same-origin'});
        const contentType = response.headers.get('content-type') || '';
        const body = contentType.includes('json') ? await response.json() : await response.text();
        if (!response.ok) {
            const error = new Error(body?.message || `Request failed (${response.status})`);
            error.status = response.status;
            throw error;
        }
        return body;
    }
    function user() { try { return JSON.parse(localStorage.getItem('aureliaUser') || 'null'); } catch { return null; } }
    function setUser(value) { localStorage.setItem('aureliaUser', JSON.stringify(value)); updateNav(); }
    function clearUser() { localStorage.removeItem('aureliaUser'); updateNav(); }
    function updateNav() {
        const current = user();
        const guest = document.getElementById('guestNav'), userMenu = document.getElementById('userNav');
        const admin = document.getElementById('adminNav');
        if (guest) guest.hidden = !!current;
        if (userMenu) userMenu.hidden = !current;
        if (admin) admin.hidden = !current || current.role !== 'ADMIN';
        const label = document.getElementById('navUserName');
        if (label && current) label.textContent = current.fullName.split(' ')[0];
    }
    function message(id, text, type = 'danger') {
        const element = document.getElementById(id);
        if (!element) return;
        element.textContent = text;
        element.className = `alert alert-${type}`;
        element.classList.remove('d-none');
    }
    document.addEventListener('click', async event => {
        const logout = event.target.closest('[data-action="logout"]');
        if (!logout) return;
        event.preventDefault();
        logout.disabled = true;
        try {
            await request('/api/auth/logout', {method:'POST'});
            clearUser();
            location.href = '/';
        } catch (error) {
            logout.disabled = false;
            if (error.status === 401) {
                clearUser();
                location.href = '/';
                return;
            }
            message('globalMessage', error.message || 'Unable to sign out. Please try again.');
        }
    });
    document.addEventListener('DOMContentLoaded', updateNav);
    return {request, csrf, user, setUser, clearUser, message, updateNav};
})();
