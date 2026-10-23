/* ============================================
   HELP DESK PI IV — Lógica de Autenticação
   ============================================ */

document.addEventListener('DOMContentLoaded', () => {
    // Referências DOM
    const loginForm = document.getElementById('loginForm');
    const loginError = document.getElementById('loginError');
    const btnLogin = document.getElementById('btnLogin');
    const passwordInput = document.getElementById('loginPassword');
    const passwordToggle = document.getElementById('togglePassword');

    passwordToggle?.addEventListener('click', () => {
        const showPassword = passwordInput.type === 'password';
        passwordInput.type = showPassword ? 'text' : 'password';
        passwordToggle.setAttribute('aria-pressed', String(showPassword));
        passwordToggle.setAttribute('aria-label', showPassword ? 'Ocultar senha' : 'Mostrar senha');
    });

    // API URL BASE
    const API_URL = 'https://help-desk-pi-iv.onrender.com';

    // Mostra mensagem de sucesso se veio do cadastro
    const params = new URLSearchParams(window.location.search);
    if (params.get('registered') === 'true') {
        const successDiv = document.createElement('div');
        successDiv.className = 'success-message';
        successDiv.textContent = 'Conta criada com sucesso! Faca login para continuar.';
        loginForm.insertBefore(successDiv, loginForm.firstChild);
    }

    // ── Handler Login ──
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const email = document.getElementById('loginEmail').value;
        const password = document.getElementById('loginPassword').value;

        btnLogin.disabled = true;
        btnLogin.textContent = 'Entrando...';
        loginError.style.display = 'none';

        try {
            const response = await fetch(`${API_URL}/api/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });

            const data = await response.json();

            if (response.ok) {
                // Sucesso: Salvar token e user no localStorage
                localStorage.setItem('token', data.token);
                localStorage.setItem('user', JSON.stringify(data.user));
                
                // Redirecionar para Dashboard
                window.location.href = 'index.html';
            } else {
                loginError.textContent = data.error || 'Erro ao realizar login. Verifique suas credenciais.';
                loginError.style.display = 'block';
            }
        } catch (error) {
            console.error('Login error:', error);
            loginError.textContent = 'Erro de conexão com o servidor. Tente novamente mais tarde.';
            loginError.style.display = 'block';
        } finally {
            btnLogin.disabled = false;
            btnLogin.textContent = 'Entrar no HelpDesk';
        }
    });
});
