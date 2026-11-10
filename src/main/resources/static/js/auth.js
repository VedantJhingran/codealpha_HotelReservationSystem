document.getElementById('loginForm')?.addEventListener('submit', async event => {
    event.preventDefault();
    const form = event.currentTarget;
    try {
        const user = await Aurelia.request('/api/auth/login', {method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({
            email: document.getElementById('loginEmail').value,
            password: document.getElementById('loginPassword').value
        })});
        Aurelia.setUser(user);
        location.href = user.role === 'ADMIN' ? '/admin' : '/rooms';
    } catch (error) { Aurelia.message('loginMessage', error.message || 'Unable to sign in.'); }
});
document.getElementById('registerForm')?.addEventListener('submit', async event => {
    event.preventDefault();
    try {
        await Aurelia.request('/api/auth/register', {method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({
            fullName: document.getElementById('registerName').value,
            email: document.getElementById('registerEmail').value,
            password: document.getElementById('registerPassword').value,
            phoneNumber: document.getElementById('registerPhone').value || null
        })});
        location.href = '/login?registered=true';
    } catch (error) { Aurelia.message('registerMessage', error.message || 'Unable to create account.'); }
});
